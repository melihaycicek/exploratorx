package com.exploratorx.stream.state;

/**
 * Central registry for all Kafka Streams RocksDB state store names.
 * Using constants prevents typo-related bugs across topologies.
 */
public final class StateStoreNames {

    private StateStoreNames() {}

    // CDR state stores
    /** Stores the last trusted CDR signal per subscriber_id. */
    public static final String CDR_SUBSCRIBER_STATE = "cdr-subscriber-state";

    // Payment state stores
    /** Stores the last trusted payment transaction per card_token. */
    public static final String PAY_CARD_STATE = "pay-card-state";

    /** Stores velocity window (transaction count) per card_token. */
    public static final String PAY_CARD_VELOCITY = "pay-card-velocity";

    /** Stores recent idempotency keys per card_token for duplicate detection. */
    public static final String PAY_IDEMPOTENCY_KEYS = "pay-idempotency-keys";
}
