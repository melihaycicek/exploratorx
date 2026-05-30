package com.exploratorx.pay.engine;

import com.exploratorx.common.scoring.RiskScore;
import com.exploratorx.pay.model.CardState;
import com.exploratorx.pay.model.PaymentTransaction;
import org.springframework.stereotype.Component;

/**
 * Detects duplicate payment transactions using idempotency keys.
 *
 * Rule:
 *   duplicate idempotency_key seen → DUPLICATE_IGNORED (+95)
 */
@Component
public class DuplicatePaymentCheck {

    /**
     * Check if this transaction is a duplicate based on idempotency key.
     *
     * @param current the incoming payment transaction
     * @param state   current card state with recent idempotency keys
     * @param score   mutable risk score accumulator
     * @return true if this is a duplicate
     */
    public boolean evaluate(PaymentTransaction current, CardState state, RiskScore score) {
        String key = current.getIdempotencyKey();
        if (key != null && state.hasSeenIdempotencyKey(key)) {
            score.add(95, String.format("Duplicate payment detected: idempotency_key=%s", key));
            return true;
        }
        return false;
    }
}
