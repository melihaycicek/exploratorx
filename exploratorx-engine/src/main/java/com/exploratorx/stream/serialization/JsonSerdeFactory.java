package com.exploratorx.stream.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Factory for creating type-safe JSON Serdes for Kafka Streams state stores.
 * Uses Jackson with JavaTimeModule for Instant/LocalDateTime support.
 */
public class JsonSerdeFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static <T> Serde<T> jsonSerde(Class<T> targetType) {
        return new Serde<>() {
            @Override
            public Serializer<T> serializer() {
                return (topic, data) -> {
                    if (data == null) return null;
                    try {
                        return OBJECT_MAPPER.writeValueAsBytes(data);
                    } catch (Exception e) {
                        throw new RuntimeException("JSON serialization failed for " + targetType.getSimpleName(), e);
                    }
                };
            }

            @Override
            public Deserializer<T> deserializer() {
                return (topic, data) -> {
                    if (data == null) return null;
                    try {
                        return OBJECT_MAPPER.readValue(data, targetType);
                    } catch (Exception e) {
                        throw new RuntimeException("JSON deserialization failed for " + targetType.getSimpleName(), e);
                    }
                };
            }
        };
    }

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }
}
