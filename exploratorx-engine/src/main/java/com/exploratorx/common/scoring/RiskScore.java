package com.exploratorx.common.scoring;

import lombok.Data;

/**
 * Mutable risk score accumulator.
 * Used during anomaly evaluation to accumulate risk points from multiple checks.
 */
@Data
public class RiskScore {

    private int score;
    private StringBuilder reasonBuilder = new StringBuilder();

    public RiskScore() {
        this.score = 0;
    }

    public RiskScore(int initialScore) {
        this.score = initialScore;
    }

    /**
     * Add points and an explanatory reason.
     */
    public RiskScore add(int points, String reason) {
        this.score += points;
        if (reasonBuilder.length() > 0) {
            reasonBuilder.append("; ");
        }
        reasonBuilder.append(reason).append(" (+").append(points).append(")");
        return this;
    }

    /**
     * Subtract points and add a reason.
     */
    public RiskScore subtract(int points, String reason) {
        this.score -= points;
        if (reasonBuilder.length() > 0) {
            reasonBuilder.append("; ");
        }
        reasonBuilder.append(reason).append(" (-").append(points).append(")");
        return this;
    }

    /**
     * Clamp score to [0, 100].
     */
    public int clampedScore() {
        return Math.max(0, Math.min(100, score));
    }

    public String getReason() {
        return reasonBuilder.toString();
    }

    public RiskLevel getLevel() {
        return RiskLevel.fromScore(clampedScore());
    }
}
