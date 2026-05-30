package com.exploratorx.pay.stream;

import com.exploratorx.pay.engine.PaymentFraudEngine;
import com.exploratorx.pay.model.CardState;
import com.exploratorx.pay.model.FraudAlertEvent;
import com.exploratorx.pay.model.PaymentTransaction;
import com.exploratorx.observability.PaymentMetrics;
import com.exploratorx.stream.state.StateStoreNames;
import com.exploratorx.websocket.LiveAnomalyPublisher;
import com.exploratorx.websocket.LiveSignalPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Stateful Kafka Streams processor for Payment transactions.
 * Reads card state from RocksDB, runs the fraud engine, updates state.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionProcessor implements Processor<String, PaymentTransaction, String, FraudAlertEvent> {

    private final PaymentFraudEngine fraudEngine;
    private final PaymentMetrics paymentMetrics;
    private final LiveSignalPublisher signalPublisher;
    private final LiveAnomalyPublisher anomalyPublisher;

    private KeyValueStore<String, CardState> cardStateStore;

    @Override
    public void init(ProcessorContext<String, FraudAlertEvent> context) {
        this.cardStateStore = context.getStateStore(StateStoreNames.PAY_CARD_STATE);
    }

    @Override
    public void process(Record<String, PaymentTransaction> record) {
        PaymentTransaction tx = record.value();
        if (tx == null) return;

        paymentMetrics.incrementPaymentEvents();
        signalPublisher.publishPaymentSignal(tx);

        // Load previous card state
        CardState previousState = cardStateStore.get(tx.getCardToken());

        // Run fraud engine
        FraudAlertEvent alert = fraudEngine.evaluate(tx, Optional.ofNullable(previousState));

        // Update card state
        if (previousState == null) {
            cardStateStore.put(tx.getCardToken(), CardState.from(tx));
        } else {
            updateCardState(previousState, tx);
            cardStateStore.put(tx.getCardToken(), previousState);
        }

        // Update metrics
        switch (alert.getFraudDecision()) {
            case BLOCKED -> {
                paymentMetrics.incrementBlockedTransactions();
                paymentMetrics.incrementFraudAlerts();
            }
            case CHALLENGE_REQUIRED -> {
                paymentMetrics.incrementChallengeRequired();
                paymentMetrics.incrementFraudAlerts();
            }
            case REVIEW_REQUIRED -> paymentMetrics.incrementFraudAlerts();
            case DUPLICATE_IGNORED -> paymentMetrics.incrementDuplicateIgnored();
            default -> {}
        }

        if (alert.isVelocityFraud()) paymentMetrics.incrementVelocityFraud();

        // Broadcast to dashboard
        if (alert.getFraudDecision().requiresAction()) {
            anomalyPublisher.publishPaymentFraud(alert);
        }
    }

    private void updateCardState(CardState state, PaymentTransaction tx) {
        // Update velocity window
        if (state.getVelocityWindowStart() != null) {
            long minutesSinceWindow = Duration.between(
                    state.getVelocityWindowStart(), tx.getEventTime()).toMinutes();
            if (minutesSinceWindow > 5) {
                // Reset velocity window
                state.setVelocityCount(1);
                state.setVelocityWindowStart(tx.getEventTime());
            } else {
                state.setVelocityCount(state.getVelocityCount() + 1);
            }
        } else {
            state.setVelocityCount(1);
            state.setVelocityWindowStart(tx.getEventTime());
        }

        // Update location and timestamps
        state.setLastCity(tx.getCity());
        state.setLastLatitude(tx.getLatitude());
        state.setLastLongitude(tx.getLongitude());
        state.setLastEventTime(tx.getEventTime());
        state.setLastTerminalId(tx.getTerminalId());
        state.setTotalTransactions(state.getTotalTransactions() + 1);
        state.setUpdatedAt(Instant.now());

        // Track idempotency key
        if (tx.getIdempotencyKey() != null) {
            state.addIdempotencyKey(tx.getIdempotencyKey());
        }
    }
}
