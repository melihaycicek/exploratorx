package com.exploratorx.cdr.model;

import com.exploratorx.cdr.enums.SignalType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

/**
 * Normalized CDR (Call Detail Record) signal event.
 * Produced after the raw Debezium envelope is unwrapped by CdrRawEventNormalizer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CdrSignal {

    /** Database record ID. */
    private Long id;

    /** Subscriber identifier (telecom subscriber). */
    private String subscriberId;

    /** When this CDR event occurred. */
    private Instant eventTime;

    /** German city name where the signal was recorded. */
    private String city;

    /** Latitude of the cell tower / signal location. */
    private double latitude;

    /** Longitude of the cell tower / signal location. */
    private double longitude;

    /** Cell tower identifier. */
    private String cellId;

    /** Type of signal (VOICE, SMS, DATA, ROAMING, HANDOVER). */
    @Builder.Default
    private SignalType signalType = SignalType.VOICE;

    /** When this record was created in the database. */
    private Instant createdAt;
}
