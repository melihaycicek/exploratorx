package com.exploratorx.api;

import com.exploratorx.persistence.AnomalyLogRepository;
import com.exploratorx.persistence.entity.AnomalyLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for querying detected anomalies.
 *
 * GET /api/anomalies          — paginated list, newest first
 * GET /api/anomalies/{id}     — single anomaly by ID
 */
@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnomalyController {

    private final AnomalyLogRepository anomalyLogRepository;

    @GetMapping
    public ResponseEntity<Page<AnomalyLogEntity>> getAnomalies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(anomalyLogRepository.findAllByOrderByDetectedAtDesc(
                PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnomalyLogEntity> getAnomaly(@PathVariable Long id) {
        return anomalyLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
