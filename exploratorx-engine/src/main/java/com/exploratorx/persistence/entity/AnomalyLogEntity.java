package com.exploratorx.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity for persisting detected anomalies.
 * Maps to the anomaly_log table in PostgreSQL.
 */
@Entity
@Table(name = "anomaly_log")
@Data
@NoArgsConstructor
public class AnomalyLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mode", nullable = false, length = 16)
    private String mode;

    @Column(name = "source_id", nullable = false, length = 128)
    private String sourceId;

    @Column(name = "entity_id", length = 128)
    private String entityId;

    @Column(name = "from_city", length = 100)
    private String fromCity;

    @Column(name = "to_city", length = 100)
    private String toCity;

    @Column(name = "from_lat")
    private Double fromLat;

    @Column(name = "from_lon")
    private Double fromLon;

    @Column(name = "to_lat")
    private Double toLat;

    @Column(name = "to_lon")
    private Double toLon;

    @Column(name = "time_diff_minutes")
    private Double timeDiffMinutes;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "required_speed_kmh")
    private Double requiredSpeedKmh;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "decision", length = 32)
    private String decision;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayload;

    @Column(name = "detected_at")
    private Instant detectedAt = Instant.now();
}
