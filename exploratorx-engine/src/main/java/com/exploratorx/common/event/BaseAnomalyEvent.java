package com.exploratorx.common.event;

import lombok.Data;

import java.time.Instant;

/**
 * Base class for all anomaly events emitted by CDR and Payment engines.
 */
@Data
public abstract class BaseAnomalyEvent {

    /** Unique identifier for this anomaly detection result. */
    private String anomalyId;

    /** The mode this event came from: CDR or PAYMENT. */
    private String mode;

    /** Source record identifier (CDR signal ID or payment transaction ID). */
    private String sourceId;

    /** Entity being tracked (subscriber_id or card_token). */
    private String entityId;

    /** City where the previous event occurred. */
    private String fromCity;

    /** City where the current event occurred. */
    private String toCity;

    /** Distance between the two events in km. */
    private double distanceKm;

    /** Time difference between the two events in minutes. */
    private double timeDiffMinutes;

    /** Required speed to physically travel this route (km/h). */
    private double requiredSpeedKmh;

    /** Final risk score [0-100]. */
    private int riskScore;

    /** Human-readable decision string. */
    private String decision;

    /** Explanation of why this anomaly was flagged. */
    private String reason;

    /** When this anomaly was detected. */
    private Instant detectedAt = Instant.now();
}
