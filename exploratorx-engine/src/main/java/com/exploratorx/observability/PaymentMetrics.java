package com.exploratorx.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Payment-specific Micrometer metrics.
 * Exposes counters for fraud decisions, blocked transactions, and velocity alerts.
 */
@Component
@RequiredArgsConstructor
public class PaymentMetrics {

    private final MeterRegistry meterRegistry;

    private Counter paymentEventsTotal;
    private Counter fraudAlertsTotal;
    private Counter blockedTransactionsTotal;
    private Counter challengeRequiredTotal;
    private Counter duplicateIgnoredTotal;
    private Counter velocityFraudTotal;

    @PostConstruct
    public void init() {
        paymentEventsTotal = Counter.builder("payment_events_total")
                .description("Total payment events processed")
                .register(meterRegistry);

        fraudAlertsTotal = Counter.builder("fraud_alerts_total")
                .description("Total fraud alerts raised")
                .register(meterRegistry);

        blockedTransactionsTotal = Counter.builder("blocked_transactions_total")
                .description("Total transactions blocked by fraud engine")
                .register(meterRegistry);

        challengeRequiredTotal = Counter.builder("challenge_required_total")
                .description("Total 3DS challenge required decisions")
                .register(meterRegistry);

        duplicateIgnoredTotal = Counter.builder("duplicate_payments_ignored_total")
                .description("Total duplicate payments ignored")
                .register(meterRegistry);

        velocityFraudTotal = Counter.builder("velocity_fraud_total")
                .description("Total velocity fraud detections")
                .register(meterRegistry);
    }

    public void incrementPaymentEvents() { paymentEventsTotal.increment(); }
    public void incrementFraudAlerts() { fraudAlertsTotal.increment(); }
    public void incrementBlockedTransactions() { blockedTransactionsTotal.increment(); }
    public void incrementChallengeRequired() { challengeRequiredTotal.increment(); }
    public void incrementDuplicateIgnored() { duplicateIgnoredTotal.increment(); }
    public void incrementVelocityFraud() { velocityFraudTotal.increment(); }
}
