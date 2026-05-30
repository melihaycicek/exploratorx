package com.exploratorx.api;

import com.exploratorx.persistence.AnomalyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API for aggregate statistics.
 *
 * GET /api/stats — global engine stats for dashboard
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatsController {

    private final AnomalyLogRepository anomalyLogRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalCdrEvents = anomalyLogRepository.countByMode("CDR");
        long totalPayEvents = anomalyLogRepository.countByMode("PAYMENT");
        long impossibleSignals = anomalyLogRepository.countByModeAndDecision("CDR", "IMPOSSIBLE_SIGNAL");
        long splitSignals = anomalyLogRepository.countByModeAndDecision("CDR", "SPLIT_SIGNAL");
        long suspiciousMovements = anomalyLogRepository.countByModeAndDecision("CDR", "SUSPICIOUS_MOVEMENT");
        long blockedTransactions = anomalyLogRepository.countByModeAndDecision("PAYMENT", "BLOCKED");
        long challengeRequired = anomalyLogRepository.countByModeAndDecision("PAYMENT", "CHALLENGE_REQUIRED");
        long duplicateIgnored = anomalyLogRepository.countByModeAndDecision("PAYMENT", "DUPLICATE_IGNORED");

        return ResponseEntity.ok(Map.of(
                "cdr", Map.of(
                        "totalEvents", totalCdrEvents,
                        "impossibleSignals", impossibleSignals,
                        "splitSignals", splitSignals,
                        "suspiciousMovements", suspiciousMovements
                ),
                "payment", Map.of(
                        "totalEvents", totalPayEvents,
                        "blockedTransactions", blockedTransactions,
                        "challengeRequired", challengeRequired,
                        "duplicateIgnored", duplicateIgnored
                )
        ));
    }
}
