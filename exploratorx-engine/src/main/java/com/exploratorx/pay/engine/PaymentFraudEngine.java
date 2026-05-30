package com.exploratorx.pay.engine;

import com.exploratorx.pay.enums.FraudDecision;
import com.exploratorx.pay.model.CardState;
import com.exploratorx.pay.model.FraudAlertEvent;
import com.exploratorx.pay.model.PaymentTransaction;
import com.exploratorx.common.scoring.RiskScore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Core Payment fraud detection engine.
 *
 * Orchestrates all Payment fraud rule checks:
 *   1. DuplicatePaymentCheck   — idempotency key collision (+95)
 *   2. ImpossibleCardTravelCheck — impossible location jump (+80)
 *   3. VelocityFraudCheck      — too many transactions in window (+50)
 *   4. GeoMismatchCheck        — POS vs IP country mismatch (+30)
 *   5. ThreeDSDecisionCheck    — new terminal/high amount (+25), 3DS failed (+45)
 *
 * Decision thresholds:
 *   0-30   → APPROVED
 *   31-60  → REVIEW_REQUIRED
 *   61-80  → CHALLENGE_REQUIRED
 *   81+    → BLOCKED
 *   duplicate → DUPLICATE_IGNORED
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFraudEngine {

    private final DuplicatePaymentCheck duplicateCheck;
    private final ImpossibleCardTravelCheck travelCheck;
    private final VelocityFraudCheck velocityCheck;
    private final GeoMismatchCheck geoMismatchCheck;
    private final ThreeDSDecisionCheck threeDSCheck;

    /**
     * Evaluate a payment transaction for fraud.
     *
     * @param current  the incoming payment transaction
     * @param previous the last known card state (empty for first transaction)
     * @return a fraud alert event
     */
    public FraudAlertEvent evaluate(PaymentTransaction current, Optional<CardState> previous) {
        RiskScore riskScore = new RiskScore();

        boolean isDuplicate = false;
        boolean isImpossibleTravel = false;
        boolean isVelocityFraud = false;
        boolean isGeoMismatch = false;

        if (previous.isPresent()) {
            CardState prev = previous.get();

            // Check duplicate
            isDuplicate = duplicateCheck.evaluate(current, prev, riskScore);
            if (isDuplicate) {
                return buildEvent(current, prev, riskScore, FraudDecision.DUPLICATE_IGNORED,
                        false, false, true, false, 0);
            }

            // Check impossible travel
            isImpossibleTravel = travelCheck.evaluate(current, prev, riskScore);

            // Check velocity
            isVelocityFraud = velocityCheck.evaluate(current, prev, riskScore);

            // Check geo mismatch
            isGeoMismatch = geoMismatchCheck.evaluate(current, riskScore);

            // Check 3DS
            threeDSCheck.evaluate(current, prev, riskScore);
        } else {
            // First transaction — check geo mismatch and 3DS only
            isGeoMismatch = geoMismatchCheck.evaluate(current, riskScore);
            threeDSCheck.evaluate(current, null, riskScore);
        }

        int finalScore = riskScore.clampedScore();
        FraudDecision decision = FraudDecision.fromScore(finalScore);

        if (decision.isFraud() || decision == FraudDecision.REVIEW_REQUIRED) {
            log.warn("Payment fraud: card={}, decision={}, score={}, reason={}",
                    current.getCardToken(), decision, finalScore, riskScore.getReason());
        }

        int velocityCount = previous.map(CardState::getVelocityCount).orElse(0);
        return buildEvent(current, previous.orElse(null), riskScore, decision,
                isImpossibleTravel, isVelocityFraud, false, isGeoMismatch, velocityCount);
    }

    private FraudAlertEvent buildEvent(PaymentTransaction tx, CardState prev,
            RiskScore riskScore, FraudDecision decision,
            boolean impossibleTravel, boolean velocityFraud,
            boolean duplicate, boolean geoMismatch, int velocityCount) {

        return FraudAlertEvent.builder()
                .anomalyId(UUID.randomUUID().toString())
                .mode("PAYMENT")
                .sourceId(tx.getTransactionId())
                .entityId(tx.getCardToken())
                .fromCity(prev != null ? prev.getLastCity() : null)
                .toCity(tx.getCity())
                .riskScore(riskScore.clampedScore())
                .decision(decision.name())
                .reason(riskScore.getReason().isEmpty() ? "Normal transaction" : riskScore.getReason())
                .detectedAt(Instant.now())
                .fraudDecision(decision)
                .transactionId(tx.getTransactionId())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .merchantName(tx.getMerchantName())
                .impossibleTravel(impossibleTravel)
                .velocityFraud(velocityFraud)
                .duplicatePayment(duplicate)
                .geoMismatch(geoMismatch)
                .velocityCount(velocityCount)
                .threeDsStatus(tx.getThreeDsStatus())
                .build();
    }
}
