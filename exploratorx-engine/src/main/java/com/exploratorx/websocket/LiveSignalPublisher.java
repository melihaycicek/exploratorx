package com.exploratorx.websocket;

import com.exploratorx.common.event.DashboardEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Publisher for live normalized CDR and Payment signal events.
 * Delegates to DashboardBroadcaster for STOMP delivery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LiveSignalPublisher {

    private final DashboardBroadcaster broadcaster;

    public void publishCdrSignal(Object cdrSignal) {
        broadcaster.broadcastSignal(cdrSignal, DashboardEnvelope.PayloadType.CDR_SIGNAL);
    }

    public void publishPaymentSignal(Object paymentTx) {
        broadcaster.broadcastSignal(paymentTx, DashboardEnvelope.PayloadType.PAYMENT_SIGNAL);
    }
}
