// src/main/java/com/edulink/backend/config/WebSocketConfig.java
package com.edulink.backend.config;

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
        // The endpoint that clients will connect to for WebSocket communication.
        // "/ws" is a common convention.
        // withSockJS() provides a fallback for browsers that don't support WebSocket.
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:3001") // Allow frontend origins
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Defines the prefix for destinations that are handled by the message broker.
        // Clients will subscribe to topics starting with "/topic".
        // For example, a client will subscribe to "/topic/chat/{conversationId}" to receive messages.
        registry.enableSimpleBroker("/topic");

        // Defines the prefix for messages that are bound for @MessageMapping-annotated methods
        // in our controllers. Clients will send messages to destinations like "/app/chat.sendMessage".
        registry.setApplicationDestinationPrefixes("/app");
    }
}