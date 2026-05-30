package com.exploratorx.pay.demo;

import com.exploratorx.pay.model.PaymentTransaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

/**
 * Writes synthetic payment transactions directly to PostgreSQL.
 * Debezium will capture the inserts via WAL and emit them to Kafka.
 *
 * Safety: Only synthetic tokenized fields are written. No real PAN, CVV, or secrets.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionWriter {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final String INSERT_SQL = """
            INSERT INTO payment_transaction
              (transaction_id, card_token, masked_pan, last4, customer_id,
               merchant_id, merchant_name, terminal_id, channel, amount, currency,
               city, country, latitude, longitude, event_time, payment_status,
               three_ds_status, device_id, ip_country, idempotency_key)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    /**
     * Write a single payment transaction to PostgreSQL.
     */
    public void write(PaymentTransaction tx) {
        try {
            jdbcTemplate.update(INSERT_SQL,
                    tx.getTransactionId(),
                    tx.getCardToken(),
                    tx.getMaskedPan(),
                    tx.getLast4(),
                    tx.getCustomerId(),
                    tx.getMerchantId(),
                    tx.getMerchantName(),
                    tx.getTerminalId(),
                    tx.getChannel() != null ? tx.getChannel().name() : "POS",
                    tx.getAmount(),
                    tx.getCurrency(),
                    tx.getCity(),
                    tx.getCountry(),
                    tx.getLatitude(),
                    tx.getLongitude(),
                    Timestamp.from(tx.getEventTime()),
                    tx.getPaymentStatus(),
                    tx.getThreeDsStatus(),
                    tx.getDeviceId(),
                    tx.getIpCountry(),
                    tx.getIdempotencyKey()
            );
            log.debug("Written payment tx={}, card={}, city={}",
                    tx.getTransactionId(), tx.getCardToken(), tx.getCity());
        } catch (Exception e) {
            log.error("Failed to write payment transaction: {}", e.getMessage(), e);
        }
    }

    /**
     * Write multiple transactions with a configurable delay between each.
     */
    public void writeSequence(List<PaymentTransaction> transactions, long delayMs) {
        for (PaymentTransaction tx : transactions) {
            write(tx);
            if (delayMs > 0) {
                try { Thread.sleep(delayMs); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }
    }
}
