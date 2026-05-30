package com.exploratorx.pay.stream;

import com.exploratorx.pay.model.PaymentTransaction;
import com.exploratorx.stream.serialization.JsonSerdeFactory;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.stereotype.Component;

/**
 * Serde for PaymentTransaction objects.
 * Uses JsonSerdeFactory for Jackson-based JSON encoding.
 */
@Component
public class PaymentTransactionSerde {

    private static final Serde<PaymentTransaction> SERDE =
            JsonSerdeFactory.jsonSerde(PaymentTransaction.class);

    public Serde<PaymentTransaction> serde() {
        return SERDE;
    }
}
