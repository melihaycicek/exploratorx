package com.exploratorx.pay.model;

import com.exploratorx.common.event.BaseAnomalyEvent;
import com.exploratorx.pay.enums.FraudDecision;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Fraud alert event produced when the Payment fraud engine detects suspicious activity.
 * Extends BaseAnomalyEvent with Payment-specific fields.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class FraudAlertEvent extends BaseAnomalyEvent {

    /** The payment-specific fraud decision. */
    private FraudDecision fraudDecision;

    /** The transaction ID that triggered this alert. */
    private String transactionId;

    /** Transaction amount. */
    private BigDecimal amount;

    /** Currency code. */
    private String currency;

    /** Merchant name at the transaction location. */
    private String merchantName;

    /** Whether this was flagged as impossible card travel. */
    private boolean impossibleTravel;

    /** Whether this was a velocity fraud detection. */
    private boolean velocityFraud;

    /** Whether this was a duplicate payment. */
    private boolean duplicatePayment;

    /** Whether there was a geo mismatch (POS country vs IP country). */
    private boolean geoMismatch;

    /** Current velocity count when detected. */
    private int velocityCount;

    /** The 3DS status if applicable. */
    private String threeDsStatus;
}
