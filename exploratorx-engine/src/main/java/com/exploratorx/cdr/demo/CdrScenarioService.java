package com.exploratorx.cdr.demo;

import com.exploratorx.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * CDR demo scenario service.
 * Triggers synthetic CDR signal sequences for dashboard demonstration.
 *
 * Each scenario writes to PostgreSQL; Debezium captures and emits to Kafka;
 * the CDR engine processes and detects anomalies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CdrScenarioService {

    private final GermanySignalFactory signalFactory;
    private final CdrSignalWriter signalWriter;
    private final AppProperties appProperties;

    /**
     * Run normal CDR flow: 5 sequential signals across random German cities.
     */
    @Async
    public void runNormalFlow() {
        String subscriberId = signalFactory.randomSubscriberId();
        long intervalMs = appProperties.getDemo().getSignalIntervalMs();
        log.info("Starting CDR Normal Flow for subscriber={}", subscriberId);

        for (int i = 0; i < 5; i++) {
            signalWriter.write(signalFactory.normalSignal(subscriberId, signalFactory.randomCity()));
            sleep(intervalMs);
        }
        log.info("CDR Normal Flow completed for subscriber={}", subscriberId);
    }

    /**
     * Run suspicious movement scenario: Berlin → Frankfurt in 15 minutes.
     */
    @Async
    public void runSuspiciousMovement() {
        String subscriberId = signalFactory.randomSubscriberId();
        long intervalMs = appProperties.getDemo().getSignalIntervalMs();
        log.info("Starting CDR Suspicious Movement for subscriber={}", subscriberId);
        signalWriter.writeSequence(signalFactory.suspiciousScenario(subscriberId), intervalMs);
    }

    /**
     * Run impossible signal scenario: Berlin → Hamburg in 1 minute.
     */
    @Async
    public void runImpossibleSignal() {
        String subscriberId = signalFactory.randomSubscriberId();
        long intervalMs = appProperties.getDemo().getSignalIntervalMs();
        log.info("Starting CDR Impossible Signal for subscriber={}", subscriberId);
        signalWriter.writeSequence(signalFactory.impossibleScenario(subscriberId), intervalMs);
    }

    /**
     * Run split signal scenario: same subscriber in Berlin + Munich within 30 seconds.
     */
    @Async
    public void runSplitSignal() {
        String subscriberId = signalFactory.randomSubscriberId();
        long intervalMs = appProperties.getDemo().getSignalIntervalMs();
        log.info("Starting CDR Split Signal for subscriber={}", subscriberId);
        signalWriter.writeSequence(signalFactory.splitSignalScenario(subscriberId), intervalMs);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
