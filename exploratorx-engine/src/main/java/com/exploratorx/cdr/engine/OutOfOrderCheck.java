package com.exploratorx.cdr.engine;

import com.exploratorx.cdr.model.CdrSignal;
import com.exploratorx.cdr.model.SubscriberState;
import com.exploratorx.common.scoring.RiskScore;
import org.springframework.stereotype.Component;

/**
 * Detects out-of-order CDR events.
 * An event is out-of-order when its timestamp is earlier than the last trusted event.
 *
 * Rule:
 *   current.eventTime < previous.lastEventTime → OUT_OF_ORDER_EVENT (+30)
 */
@Component
public class OutOfOrderCheck {

    /**
     * Evaluate whether the current signal is out of order.
     *
     * @param current  the incoming CDR signal
     * @param previous the last trusted subscriber state
     * @param score    mutable risk score accumulator
     * @return true if out-of-order
     */
    public boolean evaluate(CdrSignal current, SubscriberState previous, RiskScore score) {
        if (current.getEventTime().isBefore(previous.getLastEventTime())) {
            score.add(30, String.format(
                    "Out-of-order event: current=%s, last trusted=%s",
                    current.getEventTime(), previous.getLastEventTime()));
            return true;
        }
        return false;
    }
}
