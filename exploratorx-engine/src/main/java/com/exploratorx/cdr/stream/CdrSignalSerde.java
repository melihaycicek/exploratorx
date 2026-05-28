package com.exploratorx.cdr.stream;

import com.exploratorx.cdr.model.CdrSignal;
import com.exploratorx.stream.serialization.JsonSerdeFactory;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.stereotype.Component;

/**
 * Serde (Serializer/Deserializer) for CdrSignal objects.
 * Uses JsonSerdeFactory for Jackson-based JSON encoding.
 */
@Component
public class CdrSignalSerde {

    private static final Serde<CdrSignal> SERDE = JsonSerdeFactory.jsonSerde(CdrSignal.class);

    public Serde<CdrSignal> serde() {
        return SERDE;
    }
}
