package com.uno.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry                          // native WS, easiest for Postman
                .addEndpoint("/ws/game")
                .setAllowedOriginPatterns("*");

        registry                          // keep a *separate* SockJS endpoint if you still want fallback
                .addEndpoint("/ws/game-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for messages from server to clients (topics)
        registry.enableSimpleBroker("/topic");
        // Prefix for messages from clients to server (app destinations)
        registry.setApplicationDestinationPrefixes("/app");
    }
}
