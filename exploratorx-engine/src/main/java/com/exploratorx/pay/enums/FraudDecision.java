package com.exploratorx.pay.enums;

/**
 * Payment fraud engine decisions.
 *
 * Decision thresholds:
 *   0-30   → APPROVED
 *   31-60  → REVIEW_REQUIRED
 *   61-80  → CHALLENGE_REQUIRED (3DS)
 *   81+    → BLOCKED
 */
public enum FraudDecision {

    /** Transaction approved — no fraud detected. */
    APPROVED,

    /** Transaction flagged for manual review. */
    REVIEW_REQUIRED,

    /** 3DS challenge required before processing. */
    CHALLENGE_REQUIRED,

    /** Transaction blocked — high fraud risk. */
    BLOCKED,

    /** Duplicate transaction ignored. */
    DUPLICATE_IGNORED;

    public static FraudDecision fromScore(int score) {
        if (score >= 81) return BLOCKED;
        if (score >= 61) return CHALLENGE_REQUIRED;
        if (score >= 31) return REVIEW_REQUIRED;
        return APPROVED;
    }

    public boolean isFraud() {
        return this == BLOCKED || this == CHALLENGE_REQUIRED;
    }

    public boolean requiresAction() {
        return this != APPROVED && this != DUPLICATE_IGNORED;
    }
}
