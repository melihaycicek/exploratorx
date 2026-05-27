package com.exploratorx.cdr.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

/**
 * Represents the last known trusted state of a subscriber.
 * Stored in the CDR RocksDB state store keyed by subscriber_id.
 * Used for comparison when a new CDR signal arrives.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberState {

    /** Subscriber identifier. */
    private String subscriberId;

    /** City of the last trusted signal. */
    private String lastCity;

    /** Latitude of the last trusted signal. */
    private double lastLatitude;

    /** Longitude of the last trusted signal. */
    private double lastLongitude;

    /** Timestamp of the last trusted signal. */
    private Instant lastEventTime;

    /** Cell ID of the last signal. */
    private String lastCellId;

    /** Total signals processed for this subscriber. */
    @Builder.Default
    private long signalCount = 0L;

    /** When this state was last updated. */
    private Instant updatedAt;

    /**
     * Create SubscriberState from a CdrSignal.
     */
    public static SubscriberState from(CdrSignal signal) {
        return SubscriberState.builder()
                .subscriberId(signal.getSubscriberId())
                .lastCity(signal.getCity())
                .lastLatitude(signal.getLatitude())
                .lastLongitude(signal.getLongitude())
                .lastEventTime(signal.getEventTime())
                .lastCellId(signal.getCellId())
                .signalCount(1L)
                .updatedAt(Instant.now())
                .build();
    }
}
