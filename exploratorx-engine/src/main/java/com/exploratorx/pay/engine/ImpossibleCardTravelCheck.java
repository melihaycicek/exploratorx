package com.exploratorx.pay.engine;

import com.exploratorx.common.geo.HaversineCalculator;
import com.exploratorx.common.scoring.RiskScore;
import com.exploratorx.pay.model.CardState;
import com.exploratorx.pay.model.PaymentTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Detects impossible card travel between two payment events.
 *
 * Rule:
 *   required_speed > 500 km/h → +80 (impossible card travel)
 */
@Component
@RequiredArgsConstructor
public class ImpossibleCardTravelCheck {

    private final HaversineCalculator haversineCalculator;
    private static final double IMPOSSIBLE_SPEED_KMH = 500.0;

    /**
     * Evaluate the current transaction against the last known card state.
     *
     * @param current  the incoming payment transaction
     * @param previous the last trusted card state
     * @param score    mutable risk score accumulator
     * @return true if impossible travel was detected
     */
    public boolean evaluate(PaymentTransaction current, CardState previous, RiskScore score) {
        double distanceKm = haversineCalculator.distanceKm(
                previous.getLastLatitude(), previous.getLastLongitude(),
                current.getLatitude(), current.getLongitude()
        );

        double timeDiffMinutes = Duration.between(
                previous.getLastEventTime(), current.getEventTime()
        ).toSeconds() / 60.0;

        if (timeDiffMinutes <= 0) return false;

        double requiredSpeedKmh = haversineCalculator.requiredSpeedKmh(distanceKm, timeDiffMinutes);

        if (requiredSpeedKmh > IMPOSSIBLE_SPEED_KMH) {
            score.add(80, String.format(
                    "Impossible card travel: %.1f km/h required (%s→%s, %.1f km in %.1f min)",
                    requiredSpeedKmh, previous.getLastCity(), current.getCity(),
                    distanceKm, timeDiffMinutes));
            return true;
        }
        return false;
    }
}
