package com.exploratorx.cdr.engine;

import com.exploratorx.cdr.model.CdrSignal;
import com.exploratorx.cdr.model.SubscriberState;
import com.exploratorx.common.geo.HaversineCalculator;
import com.exploratorx.common.scoring.RiskScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Detects physically impossible subscriber travel between two CDR events.
 *
 * Rules:
 *   required_speed > 900 km/h → IMPOSSIBLE_SIGNAL (+70)
 *   required_speed > 300 km/h → SUSPICIOUS_MOVEMENT (+40)
 *   same timestamp, different city → handled by SplitSignalCheck
 */
@Component
@RequiredArgsConstructor
public class ImpossibleTravelCheck {

    private final HaversineCalculator haversineCalculator;

    private static final double IMPOSSIBLE_SPEED_KMH = 900.0;
    private static final double SUSPICIOUS_SPEED_KMH = 300.0;

    /**
     * Evaluate the current signal against the last known subscriber state.
     *
     * @param current  the incoming CDR signal
     * @param previous the last trusted subscriber state
     * @param score    the mutable risk score accumulator
     * @return computed distance in km
     */
    public double evaluate(CdrSignal current, SubscriberState previous, RiskScore score) {
        double distanceKm = haversineCalculator.distanceKm(
                previous.getLastLatitude(), previous.getLastLongitude(),
                current.getLatitude(), current.getLongitude()
        );

        double timeDiffMinutes = Duration.between(
                previous.getLastEventTime(), current.getEventTime()
        ).toMinutes();

        if (timeDiffMinutes <= 0) {
            // Time-based check handled by SplitSignalCheck / OutOfOrderCheck
            return distanceKm;
        }

        double requiredSpeedKmh = haversineCalculator.requiredSpeedKmh(distanceKm, timeDiffMinutes);

        if (requiredSpeedKmh > IMPOSSIBLE_SPEED_KMH) {
            score.add(70, String.format(
                    "Impossible travel: %.1f km/h required (%.1f km in %.1f min)",
                    requiredSpeedKmh, distanceKm, timeDiffMinutes));
        } else if (requiredSpeedKmh > SUSPICIOUS_SPEED_KMH) {
            score.add(40, String.format(
                    "Suspicious movement: %.1f km/h required (%.1f km in %.1f min)",
                    requiredSpeedKmh, distanceKm, timeDiffMinutes));
        }

        return distanceKm;
    }
}
