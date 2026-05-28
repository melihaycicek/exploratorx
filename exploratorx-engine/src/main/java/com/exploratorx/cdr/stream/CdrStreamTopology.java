package com.exploratorx.cdr.stream;

import com.exploratorx.cdr.model.CdrAnomalyEvent;
import com.exploratorx.cdr.model.CdrSignal;
import com.exploratorx.cdr.model.SubscriberState;
import com.exploratorx.config.AppProperties;
import com.exploratorx.stream.serialization.JsonSerdeFactory;
import com.exploratorx.stream.state.StateStoreNames;
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

/**
 * Kafka Streams topology for CDR processing.
 *
 * Flow:
 *   raw topic (Debezium envelope)
 *     → normalize (extract CdrSignal)
 *     → clean topic
 *     → process with CdrSignalProcessor (stateful, RocksDB)
 *     → anomalies topic
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CdrStreamTopology {

    private final CdrRawEventNormalizer normalizer;
    private final CdrSignalProcessor signalProcessor;
    private final AppProperties appProperties;

    @Autowired
    public void buildTopology(StreamsBuilder builder) {
        String rawTopic = appProperties.getTopics().getCdr().getRaw();
        String cleanTopic = appProperties.getTopics().getCdr().getClean();
        String anomaliesTopic = appProperties.getTopics().getCdr().getAnomalies();

        // Register CDR state store
        StoreBuilder<KeyValueStore<String, SubscriberState>> stateStoreBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(StateStoreNames.CDR_SUBSCRIBER_STATE),
                        Serdes.String(),
                        JsonSerdeFactory.jsonSerde(SubscriberState.class)
                );
        builder.addStateStore(stateStoreBuilder);

        // Source: raw Debezium topic
        KStream<String, String> rawStream = builder.stream(
                rawTopic,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        // Normalize: Debezium envelope → CdrSignal
        KStream<String, CdrSignal> cleanStream = rawStream
                .mapValues(rawJson -> normalizer.normalize(rawJson))
                .filter((key, signal) -> signal != null)
                .selectKey((key, signal) -> signal.getSubscriberId());

        // Publish to clean topic
        cleanStream.to(cleanTopic,
                Produced.with(Serdes.String(), JsonSerdeFactory.jsonSerde(CdrSignal.class)));

        log.info("CDR Stream Topology built: {} → {} → {}",
                rawTopic, cleanTopic, anomaliesTopic);
    }
}
