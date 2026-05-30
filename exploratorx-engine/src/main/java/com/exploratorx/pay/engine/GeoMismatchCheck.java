package com.exploratorx.pay.engine;

import com.exploratorx.common.scoring.RiskScore;
import com.exploratorx.pay.model.PaymentTransaction;
import org.springframework.stereotype.Component;

/**
 * Detects geographic mismatch between the POS country and the IP country.
 *
 * Rule:
 *   POS country != IP country → +30 (geo mismatch)
 */
@Component
public class GeoMismatchCheck {

    /**
     * Evaluate whether the transaction country matches the IP country.
     *
     * @param current the incoming payment transaction
     * @param score   mutable risk score accumulator
     * @return true if a geo mismatch was detected
     */
    public boolean evaluate(PaymentTransaction current, RiskScore score) {
        String posCountry = current.getCountry();
        String ipCountry = current.getIpCountry();

        if (posCountry != null && ipCountry != null
                && !posCountry.equalsIgnoreCase(ipCountry)) {
            score.add(30, String.format(
                    "Geo mismatch: POS country=%s, IP country=%s",
                    posCountry, ipCountry));
            return true;
        }
        return false;
    }
}
