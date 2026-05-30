package com.exploratorx.pay.demo;

import com.exploratorx.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Payment demo scenario service.
 * Triggers synthetic payment transaction sequences for dashboard demonstration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentScenarioService {

    private final GermanyPaymentFactory paymentFactory;
    private final PaymentTransactionWriter transactionWriter;
    private final AppProperties appProperties;

    /**
     * Run normal payment flow: 3 sequential normal transactions.
     */
    @Async
    public void runNormalFlow() {
        String cardToken = paymentFactory.randomCardToken();
        long intervalMs = appProperties.getDemo().getSignalIntervalMs();
        log.info("Starting Payment Normal Flow for card={}", cardToken);
        for (int i = 0; i < 3; i++) {
            transactionWriter.write(paymentFactory.normalTransaction(cardToken, paymentFactory.randomCity()));
            sleep(intervalMs);
        }
    }

    /**
     * Run impossible card travel scenario: Berlin → Hamburg in 2 minutes.
     */
    @Async
    public void runImpossibleTransaction() {
        String cardToken = paymentFactory.randomCardToken();
        long intervalMs = appProperties.getDemo().getSignalIntervalMs();
        log.info("Starting Impossible Card Travel for card={}", cardToken);
        transactionWriter.writeSequence(paymentFactory.impossibleTravelScenario(cardToken), intervalMs);
    }

    /**
     * Run duplicate payment scenario: same idempotency key twice.
     */
    @Async
    public void runDuplicatePayment() {
        String cardToken = paymentFactory.randomCardToken();
        long intervalMs = appProperties.getDemo().getSignalIntervalMs();
        log.info("Starting Duplicate Payment for card={}", cardToken);
        transactionWriter.writeSequence(paymentFactory.duplicatePaymentScenario(cardToken), intervalMs);
    }

    /**
     * Run velocity fraud scenario: 6 transactions in 3 minutes.
     */
    @Async
    public void runVelocityFraud() {
        String cardToken = paymentFactory.randomCardToken();
        long intervalMs = appProperties.getDemo().getSignalIntervalMs();
        log.info("Starting Velocity Fraud for card={}", cardToken);
        transactionWriter.writeSequence(paymentFactory.velocityFraudScenario(cardToken), intervalMs);
    }

    /**
     * Run 3DS challenge scenario: new terminal + high amount + geo mismatch.
     */
    @Async
    public void runChallengeScenario() {
        String cardToken = paymentFactory.randomCardToken();
        log.info("Starting 3DS Challenge Scenario for card={}", cardToken);
        transactionWriter.write(paymentFactory.challengeScenario(cardToken));
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
