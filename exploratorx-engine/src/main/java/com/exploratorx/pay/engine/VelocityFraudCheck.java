package com.exploratorx.pay.engine;

import com.exploratorx.common.scoring.RiskScore;
import com.exploratorx.pay.model.CardState;
import com.exploratorx.pay.model.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Detects velocity fraud: too many transactions in a short time window.
 *
 * Rule:
 *   5+ transactions in 5 minutes → +50 (velocity fraud)
 */
@Component
public class VelocityFraudCheck {

    private static final int MAX_TX_IN_WINDOW = 5;
    private static final long WINDOW_MINUTES = 5;

    /**
     * Evaluate velocity for the current transaction.
     *
     * @param current  the incoming payment transaction
     * @param state    the current card state (with velocity window info)
     * @param score    mutable risk score accumulator
     * @return true if velocity fraud was detected
     */
    public boolean evaluate(PaymentTransaction current, CardState state, RiskScore score) {
        Instant windowStart = state.getVelocityWindowStart();
        int count = state.getVelocityCount();

        if (windowStart != null) {
            long minutesSinceWindow = Duration.between(windowStart, current.getEventTime()).toMinutes();
            if (minutesSinceWindow <= WINDOW_MINUTES && count >= MAX_TX_IN_WINDOW) {
                score.add(50, String.format(
                        "Velocity fraud: %d transactions in %d minutes (limit: %d)",
                        count, minutesSinceWindow, MAX_TX_IN_WINDOW));
                return true;
            }
        }
        return false;
    }
}
