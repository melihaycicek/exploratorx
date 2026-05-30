package com.exploratorx.pay.model;

import com.exploratorx.pay.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Normalized payment transaction event.
 * Produced after the raw Debezium envelope is unwrapped by PaymentRawEventNormalizer.
 * Uses only safe synthetic fields — no PAN, CVV, or sensitive authentication data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    /** Database record ID. */
    private Long id;

    /** Unique transaction identifier. */
    private String transactionId;

    /** Tokenized card identifier (safe, synthetic). */
    private String cardToken;

    /** Masked PAN, e.g. "**** **** **** 1234". */
    private String maskedPan;

    /** Last 4 digits of card number. */
    private String last4;

    /** Synthetic customer identifier. */
    private String customerId;

    /** Synthetic merchant identifier. */
    private String merchantId;

    /** Merchant display name. */
    private String merchantName;

    /** Terminal identifier. */
    private String terminalId;

    /** Payment channel (POS, ONLINE, ATM, etc.). */
    @Builder.Default
    private PaymentChannel channel = PaymentChannel.POS;

    /** Transaction amount. */
    private BigDecimal amount;

    /** ISO 4217 currency code. */
    @Builder.Default
    private String currency = "EUR";

    /** City where the transaction occurred. */
    private String city;

    /** ISO 3166-1 country code. */
    @Builder.Default
    private String country = "DE";

    /** Latitude of the merchant/terminal location. */
    private double latitude;

    /** Longitude of the merchant/terminal location. */
    private double longitude;

    /** When this transaction occurred. */
    private Instant eventTime;

    /** Payment status (PENDING, APPROVED, BLOCKED, etc.). */
    @Builder.Default
    private String paymentStatus = "PENDING";

    /** Authorization result from the payment network. */
    private String authResult;

    /** 3DS status (PENDING, AUTHENTICATED, FAILED, etc.). */
    private String threeDsStatus;

    /** Device identifier for mobile payments. */
    private String deviceId;

    /** Country of the IP address used (for online payments). */
    private String ipCountry;

    /** Idempotency key for duplicate detection. */
    private String idempotencyKey;

    /** When this record was created. */
    private Instant createdAt;
}
