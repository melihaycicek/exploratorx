package com.exploratorx.observability;

import com.exploratorx.stream.state.StateStoreNames;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom health/status endpoint for ExploratorX.
 *
 * GET /api/health — returns overall system status including:
 *   - Kafka Streams state (CDR + Payment topologies)
 *   - PostgreSQL connectivity
 *   - Key metric counters
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final StreamsBuilderFactoryBean streamsFactory;
    private final JdbcTemplate jdbcTemplate;
    private final MeterRegistry meterRegistry;

    @Autowired
    public HealthController(
            StreamsBuilderFactoryBean streamsFactory,
            JdbcTemplate jdbcTemplate,
            MeterRegistry meterRegistry) {
        this.streamsFactory = streamsFactory;
        this.jdbcTemplate = jdbcTemplate;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", Instant.now().toString());

        // ─── Kafka Streams ──────────────────────────────────────────────────
        boolean streamsRunning = false;
        String streamsState = "UNKNOWN";
        try {
            KafkaStreams streams = streamsFactory.getKafkaStreams();
            if (streams != null) {
                streamsState = streams.state().toString();
                streamsRunning = streams.state() == KafkaStreams.State.RUNNING;
            }
        } catch (Exception e) {
            streamsState = "ERROR: " + e.getMessage();
        }
        result.put("kafkaStreams", Map.of(
                "state", streamsState,
                "running", streamsRunning
        ));

        // ─── PostgreSQL ─────────────────────────────────────────────────────
        boolean pgOk = false;
        String pgStatus = "OK";
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            pgOk = true;
        } catch (Exception e) {
            pgStatus = "ERROR: " + e.getMessage();
        }
        result.put("postgres", Map.of("connected", pgOk, "status", pgStatus));

        // ─── Metric snapshots ────────────────────────────────────────────────
        result.put("metrics", Map.of(
                "cdr", Map.of(
                        "eventsTotal",       getCounter("cdr_events_total"),
                        "impossibleSignals", getCounter("cdr_impossible_signals_total"),
                        "splitSignals",      getCounter("cdr_split_signals_total"),
                        "suspiciousMovements", getCounter("cdr_suspicious_movements_total")
                ),
                "payment", Map.of(
                        "eventsTotal",       getCounter("payment_events_total"),
                        "blockedTotal",      getCounter("payment_blocked_total"),
                        "challengeTotal",    getCounter("payment_challenge_total"),
                        "duplicateIgnored",  getCounter("payment_duplicate_ignored_total")
                )
        ));

        // ─── Overall status ──────────────────────────────────────────────────
        String status = (streamsRunning && pgOk) ? "UP" : "DEGRADED";
        result.put("status", status);

        return result;
    }

    private long getCounter(String name) {
        try {
            return (long) meterRegistry.counter(name).count();
        } catch (Exception e) {
            return -1;
        }
    }
}
