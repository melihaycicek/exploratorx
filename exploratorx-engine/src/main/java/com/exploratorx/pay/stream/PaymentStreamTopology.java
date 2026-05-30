package com.exploratorx.pay.stream;

import com.exploratorx.config.AppProperties;
import com.exploratorx.pay.engine.PaymentFraudEngine;
import com.exploratorx.pay.model.CardState;
import com.exploratorx.pay.model.FraudAlertEvent;
import com.exploratorx.pay.model.PaymentTransaction;
import com.exploratorx.observability.PaymentMetrics;
import com.exploratorx.stream.serialization.JsonSerdeFactory;
import com.exploratorx.stream.state.StateStoreNames;
import com.exploratorx.websocket.LiveAnomalyPublisher;
import com.exploratorx.websocket.LiveSignalPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Kafka Streams topology for Payment fraud processing.
 *
 * Flow:
 *   raw topic (Debezium envelope)
 *     → normalize (extract PaymentTransaction)
 *     → clean topic
 *     → evaluate with PaymentFraudEngine (stateful, RocksDB)
 *     → fraud-alerts topic
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentStreamTopology {

    private final PaymentRawEventNormalizer normalizer;
    private final PaymentFraudEngine fraudEngine;
    private final PaymentMetrics paymentMetrics;
    private final LiveSignalPublisher signalPublisher;
    private final LiveAnomalyPublisher anomalyPublisher;
    private final AppProperties appProperties;

    @Autowired
    public void buildTopology(StreamsBuilder builder) {
        String rawTopic = appProperties.getTopics().getPay().getRaw();
        String cleanTopic = appProperties.getTopics().getPay().getClean();
        String fraudAlertsTopic = appProperties.getTopics().getPay().getFraudAlerts();

        // Register Payment card state store
        StoreBuilder<KeyValueStore<String, CardState>> cardStateStoreBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(StateStoreNames.PAY_CARD_STATE),
                        Serdes.String(),
                        JsonSerdeFactory.jsonSerde(CardState.class)
                );
        builder.addStateStore(cardStateStoreBuilder);

        // Source: raw Debezium topic
        KStream<String, String> rawStream = builder.stream(
                rawTopic, Consumed.with(Serdes.String(), Serdes.String())
        );

        // Normalize: Debezium envelope → PaymentTransaction
        KStream<String, PaymentTransaction> cleanStream = rawStream
                .mapValues(rawJson -> normalizer.normalize(rawJson))
                .filter((key, tx) -> tx != null)
                .selectKey((key, tx) -> tx.getCardToken());

        // Publish to clean topic
        cleanStream.to(cleanTopic,
                Produced.with(Serdes.String(), JsonSerdeFactory.jsonSerde(PaymentTransaction.class)));

        log.info("Payment Stream Topology built: {} → {} → {}",
                rawTopic, cleanTopic, fraudAlertsTopic);
    }
}
