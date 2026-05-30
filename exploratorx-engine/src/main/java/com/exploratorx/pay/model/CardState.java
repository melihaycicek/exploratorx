package com.exploratorx.pay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Represents the current fraud tracking state for a card token.
 * Stored in the Payment RocksDB state store keyed by card_token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardState {

    /** Card token (key in state store). */
    private String cardToken;

    /** City of the last trusted transaction. */
    private String lastCity;

    /** Latitude of the last trusted transaction location. */
    private double lastLatitude;

    /** Longitude of the last trusted transaction location. */
    private double lastLongitude;

    /** Timestamp of the last trusted transaction. */
    private Instant lastEventTime;

    /** Last terminal ID used. */
    private String lastTerminalId;

    /** Transaction count in the current velocity window. */
    @Builder.Default
    private int velocityCount = 0;

    /** Start of the current velocity window. */
    private Instant velocityWindowStart;

    /** Total transactions processed for this card. */
    @Builder.Default
    private long totalTransactions = 0L;

    /** Recent idempotency keys for duplicate detection (last 20). */
    @Builder.Default
    private Queue<String> recentIdempotencyKeys = new LinkedList<>();

    /** When this state was last updated. */
    private Instant updatedAt;

    /**
     * Create CardState from a PaymentTransaction (first transaction).
     */
    public static CardState from(PaymentTransaction tx) {
        return CardState.builder()
                .cardToken(tx.getCardToken())
                .lastCity(tx.getCity())
                .lastLatitude(tx.getLatitude())
                .lastLongitude(tx.getLongitude())
                .lastEventTime(tx.getEventTime())
                .lastTerminalId(tx.getTerminalId())
                .velocityCount(1)
                .velocityWindowStart(tx.getEventTime())
                .totalTransactions(1L)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Check if the given idempotency key was recently seen.
     */
    public boolean hasSeenIdempotencyKey(String key) {
        return key != null && recentIdempotencyKeys.contains(key);
    }

    /**
     * Add an idempotency key to the recent keys ring buffer (max 20).
     */
    public void addIdempotencyKey(String key) {
        if (key == null) return;
        if (recentIdempotencyKeys.size() >= 20) {
            recentIdempotencyKeys.poll();
        }
        recentIdempotencyKeys.add(key);
    }
}
