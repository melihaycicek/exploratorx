package com.exploratorx.websocket;

import com.exploratorx.common.event.DashboardEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Publisher for live anomaly and fraud alert events.
 * Delegates to DashboardBroadcaster for STOMP delivery to /topic/live-anomalies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LiveAnomalyPublisher {

    private final DashboardBroadcaster broadcaster;

    public void publishCdrAnomaly(Object cdrAnomaly) {
        broadcaster.broadcastAnomaly(cdrAnomaly, DashboardEnvelope.PayloadType.CDR_ANOMALY);
        log.info("CDR anomaly published to dashboard: {}", cdrAnomaly);
    }

    public void publishPaymentFraud(Object fraudAlert) {
        broadcaster.broadcastAnomaly(fraudAlert, DashboardEnvelope.PayloadType.PAYMENT_FRAUD);
        log.info("Payment fraud alert published to dashboard: {}", fraudAlert);
    }
}
