package com.exploratorx.cdr.model;

import com.exploratorx.cdr.enums.CdrDecision;
import com.exploratorx.common.event.BaseAnomalyEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Anomaly event produced when the CDR mobility engine detects a suspicious signal.
 * Extends BaseAnomalyEvent with CDR-specific fields.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class CdrAnomalyEvent extends BaseAnomalyEvent {

    /** The CDR-specific decision type. */
    private CdrDecision cdrDecision;

    /** Cell ID of the current signal. */
    private String cellId;

    /** Cell ID of the previous trusted signal. */
    private String previousCellId;

    /** The current signal's latitude. */
    private double toLatitude;

    /** The current signal's longitude. */
    private double toLongitude;

    /** The previous signal's latitude. */
    private double fromLatitude;

    /** The previous signal's longitude. */
    private double fromLongitude;

    /** Whether this event represents a split signal (simultaneous location). */
    private boolean splitSignal;
}
