package com.exploratorx.cdr.engine;

import com.exploratorx.cdr.enums.CdrDecision;
import com.exploratorx.cdr.model.CdrAnomalyEvent;
import com.exploratorx.cdr.model.CdrSignal;
import com.exploratorx.cdr.model.SubscriberState;
import com.exploratorx.common.scoring.RiskLevel;
import com.exploratorx.common.scoring.RiskScore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Core CDR mobility anomaly engine.
 *
 * Orchestrates all CDR rule checks:
 *   1. SplitSignalCheck  — simultaneous location (+90)
 *   2. OutOfOrderCheck   — timestamp out of order (+30)
 *   3. ImpossibleTravelCheck — speed based (+40 or +70)
 *
 * Decision thresholds:
 *   0-30   → NORMAL
 *   31-60  → SUSPICIOUS_MOVEMENT
 *   61-89  → SUSPICIOUS_MOVEMENT_HIGH
 *   90+    → IMPOSSIBLE_SIGNAL or SPLIT_SIGNAL
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CdrMobilityEngine {

    private final SplitSignalCheck splitSignalCheck;
    private final OutOfOrderCheck outOfOrderCheck;
    private final ImpossibleTravelCheck impossibleTravelCheck;

    /**
     * Evaluate a new CDR signal against the previous subscriber state.
     *
     * @param current  the incoming normalized CDR signal
     * @param previous the last trusted subscriber state (may be empty for first signal)
     * @return an anomaly event if a decision was made, empty for first-time subscribers
     */
    public Optional<CdrAnomalyEvent> evaluate(CdrSignal current, Optional<SubscriberState> previous) {
        if (previous.isEmpty()) {
            log.debug("First signal for subscriber {}, establishing baseline", current.getSubscriberId());
            return Optional.empty();
        }

        SubscriberState prev = previous.get();
        RiskScore riskScore = new RiskScore();

        // Run all checks
        boolean isSplit = splitSignalCheck.evaluate(current, prev, riskScore);
        boolean isOutOfOrder = outOfOrderCheck.evaluate(current, prev, riskScore);
        double distanceKm = 0.0;

        if (!isOutOfOrder) {
            distanceKm = impossibleTravelCheck.evaluate(current, prev, riskScore);
        }

        int finalScore = riskScore.clampedScore();
        CdrDecision decision = resolveDecision(finalScore, isSplit, isOutOfOrder);

        double timeDiffMinutes = Duration.between(prev.getLastEventTime(), current.getEventTime()).toSeconds() / 60.0;

        CdrAnomalyEvent event = CdrAnomalyEvent.builder()
                .anomalyId(UUID.randomUUID().toString())
                .mode("CDR")
                .sourceId(String.valueOf(current.getId()))
                .entityId(current.getSubscriberId())
                .fromCity(prev.getLastCity())
                .toCity(current.getCity())
                .fromLatitude(prev.getLastLatitude())
                .fromLongitude(prev.getLastLongitude())
                .toLatitude(current.getLatitude())
                .toLongitude(current.getLongitude())
                .distanceKm(distanceKm)
                .timeDiffMinutes(timeDiffMinutes)
                .riskScore(finalScore)
                .decision(decision.name())
                .reason(riskScore.getReason().isEmpty() ? "Normal movement" : riskScore.getReason())
                .detectedAt(Instant.now())
                .cdrDecision(decision)
                .splitSignal(isSplit)
                .cellId(current.getCellId())
                .previousCellId(prev.getLastCellId())
                .build();

        if (decision.isAnomaly()) {
            log.warn("CDR anomaly detected: subscriber={}, decision={}, score={}, reason={}",
                    current.getSubscriberId(), decision, finalScore, riskScore.getReason());
        } else {
            log.debug("CDR normal: subscriber={}, city={}", current.getSubscriberId(), current.getCity());
        }

        return Optional.of(event);
    }

    private CdrDecision resolveDecision(int score, boolean isSplit, boolean isOutOfOrder) {
        if (isOutOfOrder) return CdrDecision.OUT_OF_ORDER_EVENT;
        if (isSplit || score >= 90) return isSplit ? CdrDecision.SPLIT_SIGNAL : CdrDecision.IMPOSSIBLE_SIGNAL;
        if (score >= 61) return CdrDecision.SUSPICIOUS_MOVEMENT_HIGH;
        if (score >= 31) return CdrDecision.SUSPICIOUS_MOVEMENT;
        return CdrDecision.NORMAL;
    }
}
