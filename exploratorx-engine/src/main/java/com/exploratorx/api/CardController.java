package com.exploratorx.api;

import com.exploratorx.persistence.AnomalyLogRepository;
import com.exploratorx.persistence.entity.AnomalyLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for card payment timeline.
 *
 * GET /api/cards/{token}/timeline — timeline of fraud alerts for a card token
 */
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CardController {

    private final AnomalyLogRepository anomalyLogRepository;

    @GetMapping("/{token}/timeline")
    public ResponseEntity<List<AnomalyLogEntity>> getCardTimeline(@PathVariable String token) {
        List<AnomalyLogEntity> timeline = anomalyLogRepository
                .findByEntityIdOrderByDetectedAtDesc(token);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/{token}/stats")
    public ResponseEntity<Map<String, Object>> getCardStats(@PathVariable String token) {
        List<AnomalyLogEntity> timeline = anomalyLogRepository
                .findByEntityIdOrderByDetectedAtDesc(token);
        long fraudCount = timeline.stream()
                .filter(e -> "BLOCKED".equals(e.getDecision())
                        || "CHALLENGE_REQUIRED".equals(e.getDecision())).count();
        return ResponseEntity.ok(Map.of(
                "cardToken", token,
                "totalTransactions", timeline.size(),
                "fraudAlerts", fraudCount
        ));
    }
}
