package com.exploratorx.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * CDR-specific Micrometer metrics.
 * Exposes counters for events, anomaly types, and detection latency.
 */
@Component
@RequiredArgsConstructor
public class CdrMetrics {

    private final MeterRegistry meterRegistry;

    private Counter cdrEventsTotal;
    private Counter impossibleSignalsTotal;
    private Counter suspiciousMovementsTotal;
    private Counter splitSignalsTotal;
    private Counter outOfOrderEventsTotal;
    private Timer detectionLatency;

    @PostConstruct
    public void init() {
        cdrEventsTotal = Counter.builder("cdr_events_total")
                .description("Total CDR events processed")
                .register(meterRegistry);

        impossibleSignalsTotal = Counter.builder("impossible_signals_total")
                .description("Total impossible signal anomalies detected")
                .register(meterRegistry);

        suspiciousMovementsTotal = Counter.builder("suspicious_movements_total")
                .description("Total suspicious movement events detected")
                .register(meterRegistry);

        splitSignalsTotal = Counter.builder("split_signals_total")
                .description("Total split signal anomalies detected")
                .register(meterRegistry);

        outOfOrderEventsTotal = Counter.builder("out_of_order_events_total")
                .description("Total out-of-order CDR events detected")
                .register(meterRegistry);

        detectionLatency = Timer.builder("anomaly_detection_latency_ms")
                .description("CDR anomaly detection latency in milliseconds")
                .register(meterRegistry);
    }

    public void incrementCdrEvents() { cdrEventsTotal.increment(); }
    public void incrementImpossibleSignals() { impossibleSignalsTotal.increment(); }
    public void incrementSuspiciousMovements() { suspiciousMovementsTotal.increment(); }
    public void incrementSplitSignals() { splitSignalsTotal.increment(); }
    public void incrementOutOfOrderEvents() { outOfOrderEventsTotal.increment(); }

    public void recordDetectionLatency(long milliseconds) {
        detectionLatency.record(milliseconds, TimeUnit.MILLISECONDS);
    }
}
