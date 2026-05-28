package com.exploratorx.cdr.stream;

import com.exploratorx.cdr.model.CdrSignal;
import com.exploratorx.cdr.enums.SignalType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Normalizes raw Debezium CDC envelope messages into CdrSignal objects.
 *
 * Debezium envelope structure (simplified):
 * {
 *   "payload": {
 *     "after": {
 *       "id": 1, "subscriber_id": "...", "event_time": "...", "city": "...",
 *       "latitude": 52.52, "longitude": 13.41, ...
 *     },
 *     "op": "c" | "u" | "r"
 *   }
 * }
 *
 * Only INSERT ("c") and READ ("r") operations are processed.
 * UPDATE ("u") and DELETE ("d") operations are skipped.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CdrRawEventNormalizer {

    private final ObjectMapper objectMapper;

    /**
     * Parse a raw Debezium JSON message into a CdrSignal.
     *
     * @param rawJson the raw Debezium envelope JSON
     * @return normalized CdrSignal, or null if the message should be skipped
     */
    public CdrSignal normalize(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode payload = root.path("payload");

            String op = payload.path("op").asText("");
            // Only process inserts and reads (snapshot)
            if (!"c".equals(op) && !"r".equals(op)) {
                log.debug("Skipping Debezium op={}", op);
                return null;
            }

            JsonNode after = payload.path("after");
            if (after.isMissingNode()) {
                log.warn("Missing 'after' node in Debezium payload");
                return null;
            }

            return CdrSignal.builder()
                    .id(after.path("id").asLong())
                    .subscriberId(after.path("subscriber_id").asText())
                    .eventTime(Instant.parse(after.path("event_time").asText()))
                    .city(after.path("city").asText())
                    .latitude(after.path("latitude").asDouble())
                    .longitude(after.path("longitude").asDouble())
                    .cellId(after.path("cell_id").asText(null))
                    .signalType(SignalType.valueOf(
                            after.path("signal_type").asText("VOICE").toUpperCase()))
                    .createdAt(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to normalize CDR raw event: {}", e.getMessage(), e);
            return null;
        }
    }
}
