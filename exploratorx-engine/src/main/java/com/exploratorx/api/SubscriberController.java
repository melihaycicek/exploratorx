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
 * REST API for subscriber CDR timeline.
 *
 * GET /api/subscribers/{id}/timeline — timeline of anomalies for a subscriber
 */
@RestController
@RequestMapping("/api/subscribers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubscriberController {

    private final AnomalyLogRepository anomalyLogRepository;

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<AnomalyLogEntity>> getSubscriberTimeline(@PathVariable String id) {
        List<AnomalyLogEntity> timeline = anomalyLogRepository
                .findByEntityIdOrderByDetectedAtDesc(id);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getSubscriberStats(@PathVariable String id) {
        List<AnomalyLogEntity> timeline = anomalyLogRepository
                .findByEntityIdOrderByDetectedAtDesc(id);
        long anomalyCount = timeline.stream()
                .filter(e -> !"NORMAL".equals(e.getDecision())).count();
        return ResponseEntity.ok(Map.of(
                "subscriberId", id,
                "totalEvents", timeline.size(),
                "anomalyCount", anomalyCount
        ));
    }
}
