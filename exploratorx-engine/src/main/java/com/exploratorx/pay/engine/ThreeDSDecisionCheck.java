package com.exploratorx.pay.engine;

import com.exploratorx.common.scoring.RiskScore;
import com.exploratorx.pay.model.CardState;
import com.exploratorx.pay.model.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Evaluates 3DS (3D Secure) challenge scenarios and new terminal high-amount risks.
 *
 * Rules:
 *   new terminal + high amount (>500 EUR) → +25
 *   3DS authentication failed             → +45
 */
@Component
public class ThreeDSDecisionCheck {

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("500.00");

    /**
     * Evaluate 3DS-related risk factors.
     *
     * @param current  the incoming payment transaction
     * @param previous the last known card state
     * @param score    mutable risk score accumulator
     */
    public void evaluate(PaymentTransaction current, CardState previous, RiskScore score) {
        // New terminal + high amount
        if (previous != null && current.getTerminalId() != null
                && !current.getTerminalId().equals(previous.getLastTerminalId())) {
            if (current.getAmount() != null
                    && current.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
                score.add(25, String.format(
                        "New terminal + high amount: terminal=%s, amount=%.2f %s",
                        current.getTerminalId(), current.getAmount(), current.getCurrency()));
            }
        }

        // 3DS failed
        if ("FAILED".equalsIgnoreCase(current.getThreeDsStatus())) {
            score.add(45, "3DS authentication failed");
        }
    }
}
