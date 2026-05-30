package com.exploratorx.pay.stream;

import com.exploratorx.pay.model.PaymentTransaction;
import com.exploratorx.pay.enums.PaymentChannel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Normalizes raw Debezium CDC envelope messages into PaymentTransaction objects.
 *
 * Only processes INSERT ("c") and READ ("r") operations.
 * All sensitive fields are read from synthetic columns only (card_token, masked_pan, last4).
 * Never processes real PAN, CVV, or authentication secrets.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRawEventNormalizer {

    private final ObjectMapper objectMapper;

    /**
     * Parse a raw Debezium JSON message into a PaymentTransaction.
     *
     * @param rawJson the raw Debezium envelope JSON
     * @return normalized PaymentTransaction, or null if the message should be skipped
     */
    public PaymentTransaction normalize(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode payload = root.path("payload");

            String op = payload.path("op").asText("");
            if (!"c".equals(op) && !"r".equals(op)) {
                log.debug("Skipping Debezium op={} for payment", op);
                return null;
            }

            JsonNode after = payload.path("after");
            if (after.isMissingNode()) {
                log.warn("Missing 'after' node in payment Debezium payload");
                return null;
            }

            String channelStr = after.path("channel").asText("POS").toUpperCase();
            PaymentChannel channel;
            try {
                channel = PaymentChannel.valueOf(channelStr);
            } catch (IllegalArgumentException e) {
                channel = PaymentChannel.POS;
            }

            return PaymentTransaction.builder()
                    .id(after.path("id").asLong())
                    .transactionId(after.path("transaction_id").asText())
                    .cardToken(after.path("card_token").asText())
                    .maskedPan(after.path("masked_pan").asText(null))
                    .last4(after.path("last4").asText(null))
                    .customerId(after.path("customer_id").asText())
                    .merchantId(after.path("merchant_id").asText(null))
                    .merchantName(after.path("merchant_name").asText(null))
                    .terminalId(after.path("terminal_id").asText(null))
                    .channel(channel)
                    .amount(new BigDecimal(after.path("amount").asText("0")))
                    .currency(after.path("currency").asText("EUR"))
                    .city(after.path("city").asText())
                    .country(after.path("country").asText("DE"))
                    .latitude(after.path("latitude").asDouble())
                    .longitude(after.path("longitude").asDouble())
                    .eventTime(Instant.parse(after.path("event_time").asText()))
                    .paymentStatus(after.path("payment_status").asText("PENDING"))
                    .authResult(after.path("auth_result").asText(null))
                    .threeDsStatus(after.path("three_ds_status").asText(null))
                    .deviceId(after.path("device_id").asText(null))
                    .ipCountry(after.path("ip_country").asText(null))
                    .idempotencyKey(after.path("idempotency_key").asText(null))
                    .createdAt(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to normalize payment raw event: {}", e.getMessage(), e);
            return null;
        }
    }
}
