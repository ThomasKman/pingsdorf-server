package com.pingsdorf.server.realtime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Broadcasts realtime events to the household-scoped STOMP topic.
 *
 * <p>Payload format: {@code { "event": "ping:created", "data": {...} }}.</p>
 */
@Service
public class RealtimePublisher {

    private final SimpMessagingTemplate broker;

    public RealtimePublisher(SimpMessagingTemplate broker) {
        this.broker = broker;
    }

    public void publish(String householdId, String event, Object data) {
        broker.convertAndSend("/topic/household." + householdId, new Envelope(event, data));
    }

    public record Envelope(String event, Object data) {}
}
