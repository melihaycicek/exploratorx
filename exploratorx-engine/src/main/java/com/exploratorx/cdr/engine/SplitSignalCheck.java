package com.exploratorx.cdr.engine;

import com.exploratorx.cdr.model.CdrSignal;
import com.exploratorx.cdr.model.SubscriberState;
import com.exploratorx.common.scoring.RiskScore;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Detects simultaneous CDR signals from the same subscriber in different cities.
 *
 * Split signal rule:
 *   same subscriber, different city, time diff <= split-signal-window-seconds → SPLIT_SIGNAL (+90)
 */
@Component
public class SplitSignalCheck {

    private static final long SPLIT_WINDOW_SECONDS = 60L;

    /**
     * Evaluate whether the current signal is a split of the previous.
     *
     * @param current  the incoming CDR signal
     * @param previous the last trusted subscriber state
     * @param score    mutable risk score accumulator
     * @return true if a split signal was detected
     */
    public boolean evaluate(CdrSignal current, SubscriberState previous, RiskScore score) {
        // Different city required for split signal
        if (current.getCity().equalsIgnoreCase(previous.getLastCity())) {
            return false;
        }

        long timeDiffSeconds = Math.abs(
                Duration.between(previous.getLastEventTime(), current.getEventTime()).toSeconds()
        );

        if (timeDiffSeconds <= SPLIT_WINDOW_SECONDS) {
            score.add(90, String.format(
                    "SPLIT_SIGNAL: %s → %s within %d seconds (impossible simultaneous location)",
                    previous.getLastCity(), current.getCity(), timeDiffSeconds));
            return true;
        }

        return false;
    }
}
