package com.exploratorx.cdr.demo;

import com.exploratorx.cdr.model.CdrSignal;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

/**
 * Writes synthetic CDR signals directly to PostgreSQL.
 * Debezium will capture the inserts via WAL and emit them to Kafka.
 *
 * This is the correct demo pattern:
 *   Demo → INSERT to PostgreSQL → Debezium WAL → Kafka raw topic → Engine
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CdrSignalWriter {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final String INSERT_SQL = """
            INSERT INTO cdr_signal
              (subscriber_id, event_time, city, latitude, longitude, cell_id, signal_type)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    /**
     * Write a single CDR signal to PostgreSQL.
     */
    public void write(CdrSignal signal) {
        try {
            jdbcTemplate.update(INSERT_SQL,
                    signal.getSubscriberId(),
                    Timestamp.from(signal.getEventTime()),
                    signal.getCity(),
                    signal.getLatitude(),
                    signal.getLongitude(),
                    signal.getCellId(),
                    signal.getSignalType().name()
            );
            log.debug("Written CDR signal: subscriber={}, city={}", signal.getSubscriberId(), signal.getCity());
        } catch (Exception e) {
            log.error("Failed to write CDR signal: {}", e.getMessage(), e);
        }
    }

    /**
     * Write multiple CDR signals with a configurable delay between each.
     */
    public void writeSequence(List<CdrSignal> signals, long delayMs) {
        for (CdrSignal signal : signals) {
            write(signal);
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
