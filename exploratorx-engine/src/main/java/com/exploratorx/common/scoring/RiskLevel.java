package com.exploratorx.common.scoring;

/**
 * Risk level classification based on numeric risk score.
 *
 * CDR thresholds:
 *   0-30   NORMAL
 *   31-60  SUSPICIOUS
 *   61-89  HIGH
 *   90+    CRITICAL
 *
 * Payment thresholds:
 *   0-30   APPROVED
 *   31-60  REVIEW_REQUIRED
 *   61-80  CHALLENGE_REQUIRED
 *   81+    BLOCKED
 */
public enum RiskLevel {
    NORMAL,
    SUSPICIOUS,
    HIGH,
    CRITICAL;

    public static RiskLevel fromScore(int score) {
        if (score >= 90) return CRITICAL;
        if (score >= 61) return HIGH;
        if (score >= 31) return SUSPICIOUS;
        return NORMAL;
    }

    public boolean isCritical() {
        return this == CRITICAL;
    }

    public boolean isAtLeastSuspicious() {
        return this == SUSPICIOUS || this == HIGH || this == CRITICAL;
    }
}
