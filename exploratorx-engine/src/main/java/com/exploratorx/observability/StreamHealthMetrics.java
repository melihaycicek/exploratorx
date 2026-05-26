package com.exploratorx.observability;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Kafka Streams health and lag metrics exposed via Micrometer.
 */
@Component
@RequiredArgsConstructor
public class StreamHealthMetrics {

    private final MeterRegistry meterRegistry;
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    private final AtomicLong eventsPerSecond = new AtomicLong(0);
    private final AtomicLong kafkaStreamLag = new AtomicLong(0);

    @PostConstruct
    public void init() {
        Gauge.builder("events_per_second", eventsPerSecond, AtomicLong::get)
                .description("Current event throughput per second")
                .register(meterRegistry);

        Gauge.builder("kafka_stream_lag", kafkaStreamLag, AtomicLong::get)
                .description("Estimated Kafka Streams consumer lag")
                .register(meterRegistry);
    }

    public void setEventsPerSecond(long eps) { eventsPerSecond.set(eps); }
    public void setKafkaStreamLag(long lag) { kafkaStreamLag.set(lag); }
}
