package com.exploratorx.cdr.stream;

import com.exploratorx.cdr.engine.CdrMobilityEngine;
import com.exploratorx.cdr.model.CdrAnomalyEvent;
import com.exploratorx.cdr.model.CdrSignal;
import com.exploratorx.cdr.model.SubscriberState;
import com.exploratorx.observability.CdrMetrics;
import com.exploratorx.stream.serialization.JsonSerdeFactory;
import com.exploratorx.stream.state.StateStoreNames;
import com.exploratorx.websocket.LiveAnomalyPublisher;
import com.exploratorx.websocket.LiveSignalPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Stateful Kafka Streams processor for CDR signals.
 * Reads subscriber state from RocksDB, runs the CDR engine, updates state.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CdrSignalProcessor implements Processor<String, CdrSignal, String, CdrAnomalyEvent> {

    private final CdrMobilityEngine mobilityEngine;
    private final CdrMetrics cdrMetrics;
    private final LiveSignalPublisher signalPublisher;
    private final LiveAnomalyPublisher anomalyPublisher;
    private final ObjectMapper objectMapper;

    private KeyValueStore<String, SubscriberState> stateStore;

    @Override
    public void init(ProcessorContext<String, CdrAnomalyEvent> context) {
        this.stateStore = context.getStateStore(StateStoreNames.CDR_SUBSCRIBER_STATE);
    }

    @Override
    public void process(Record<String, CdrSignal> record) {
        CdrSignal signal = record.value();
        if (signal == null) return;

        long startTime = System.currentTimeMillis();
        cdrMetrics.incrementCdrEvents();

        // Publish live signal to dashboard
        signalPublisher.publishCdrSignal(signal);

        // Load previous subscriber state
        SubscriberState previousState = stateStore.get(signal.getSubscriberId());

        // Run anomaly engine
        Optional<CdrAnomalyEvent> anomalyOpt = mobilityEngine.evaluate(
                signal, Optional.ofNullable(previousState));

        // Update state store with current signal (only if not out-of-order)
        if (previousState == null) {
            stateStore.put(signal.getSubscriberId(), SubscriberState.from(signal));
        } else if (!signal.getEventTime().isBefore(previousState.getLastEventTime())) {
            SubscriberState updated = SubscriberState.from(signal);
            updated.setSignalCount(previousState.getSignalCount() + 1);
            stateStore.put(signal.getSubscriberId(), updated);
        }

        // Update metrics and broadcast
        anomalyOpt.ifPresent(anomaly -> {
            switch (anomaly.getCdrDecision()) {
                case IMPOSSIBLE_SIGNAL -> cdrMetrics.incrementImpossibleSignals();
                case SPLIT_SIGNAL -> cdrMetrics.incrementSplitSignals();
                case SUSPICIOUS_MOVEMENT, SUSPICIOUS_MOVEMENT_HIGH -> cdrMetrics.incrementSuspiciousMovements();
                case OUT_OF_ORDER_EVENT -> cdrMetrics.incrementOutOfOrderEvents();
                default -> {}
            }
            if (anomaly.getCdrDecision().isAnomaly()) {
                anomalyPublisher.publishCdrAnomaly(anomaly);
            }
        });

        cdrMetrics.recordDetectionLatency(System.currentTimeMillis() - startTime);
    }
}
