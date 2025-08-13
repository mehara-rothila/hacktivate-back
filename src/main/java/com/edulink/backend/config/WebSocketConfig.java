// File Path: src/main/java/com/edulink/backend/config/WebSocketConfig.java
package com.edulink.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
@RequiredArgsConstructor // ✅ ADD THIS ANNOTATION
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // ✅ INJECT THE INTERCEPTOR
    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        log.info("🔧 Configuring simple message broker...");
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        log.info("✅ Message broker configured successfully");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("🔧 Registering WebSocket endpoints...");
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
        log.info("✅ WebSocket endpoints registered: /ws");
    }

    // ✅ ADD THIS METHOD TO REGISTER THE INTERCEPTOR
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        log.info("🔧 Registering JWT channel interceptor for WebSockets...");
        registration.interceptors(jwtChannelInterceptor);
        log.info("✅ JWT channel interceptor registered.");
    }
}
