package com.exploratorx.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * WebSocket dashboard broadcast envelope.
 * Wraps any payload (signal, anomaly, stats) with type metadata for the frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEnvelope {

    public enum PayloadType {
        CDR_SIGNAL,
        CDR_ANOMALY,
        PAYMENT_SIGNAL,
        PAYMENT_FRAUD,
        STATS_UPDATE,
        DEMO_STARTED,
        DEMO_RESET
    }

    /** Payload type tag for frontend routing. */
    private PayloadType type;

    /** The actual event payload (JSON-serialized object). */
    private Object payload;

    /** When this envelope was created. */
    private Instant timestamp = Instant.now();

    public static DashboardEnvelope of(PayloadType type, Object payload) {
        return new DashboardEnvelope(type, payload, Instant.now());
    }
}
