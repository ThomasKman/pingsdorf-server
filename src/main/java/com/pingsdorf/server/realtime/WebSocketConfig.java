package com.pingsdorf.server.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Enables a STOMP-over-WebSocket broker so the backend can broadcast
 * household-scoped events to connected frontends.
 *
 * <p>Clients connect to {@code /ws} (SockJS-compatible) and subscribe to
 * {@code /topic/household.{householdId}}.</p>
 *
 * <p>Authentication for the WebSocket transport is intentionally not enforced
 * yet — the topic namespace exposes only event payloads, not bulk data, and
 * REST endpoints remain authenticated. Real handshake auth (JWT in
 * connect-headers) is a follow-up.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
