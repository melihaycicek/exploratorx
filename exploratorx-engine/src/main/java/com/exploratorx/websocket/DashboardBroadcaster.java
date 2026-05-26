package com.exploratorx.websocket;

import com.exploratorx.common.event.DashboardEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Central WebSocket broadcaster for the ExploratorX dashboard.
 * Sends structured DashboardEnvelope messages to STOMP topics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public static final String TOPIC_LIVE_SIGNALS  = "/topic/live-signals";
    public static final String TOPIC_LIVE_ANOMALIES = "/topic/live-anomalies";
    public static final String TOPIC_STATS          = "/topic/stats";

    /**
     * Broadcast a live signal event (CDR or Payment normalized event).
     */
    public void broadcastSignal(Object payload, DashboardEnvelope.PayloadType type) {
        DashboardEnvelope envelope = DashboardEnvelope.of(type, payload);
        messagingTemplate.convertAndSend(TOPIC_LIVE_SIGNALS, envelope);
        log.debug("Broadcast signal [{}] to {}", type, TOPIC_LIVE_SIGNALS);
    }

    /**
     * Broadcast an anomaly/fraud alert event.
     */
    public void broadcastAnomaly(Object payload, DashboardEnvelope.PayloadType type) {
        DashboardEnvelope envelope = DashboardEnvelope.of(type, payload);
        messagingTemplate.convertAndSend(TOPIC_LIVE_ANOMALIES, envelope);
        log.info("Broadcast anomaly [{}] to {}", type, TOPIC_LIVE_ANOMALIES);
    }

    /**
     * Broadcast stats update.
     */
    public void broadcastStats(Object statsPayload) {
        DashboardEnvelope envelope = DashboardEnvelope.of(
                DashboardEnvelope.PayloadType.STATS_UPDATE, statsPayload);
        messagingTemplate.convertAndSend(TOPIC_STATS, envelope);
    }
}
