package com.exploratorx.cdr.enums;

/**
 * CDR anomaly detection decisions.
 * Ordered by severity (ascending).
 */
public enum CdrDecision {

    /** Normal signal — no anomaly detected. */
    NORMAL,

    /** Subscriber moved suspiciously fast (300-900 km/h). */
    SUSPICIOUS_MOVEMENT,

    /** Subscriber moved at very high speed (>300 km/h, escalated). */
    SUSPICIOUS_MOVEMENT_HIGH,

    /** Physically impossible movement detected (>900 km/h). */
    IMPOSSIBLE_SIGNAL,

    /** Same subscriber appears in two cities simultaneously. */
    SPLIT_SIGNAL,

    /** Event timestamp is earlier than the last known event. */
    OUT_OF_ORDER_EVENT;

    public boolean isAnomaly() {
        return this != NORMAL;
    }

    public boolean isCritical() {
        return this == IMPOSSIBLE_SIGNAL || this == SPLIT_SIGNAL;
    }
}
