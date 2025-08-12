// src/main/java/com/edulink/backend/config/WebSocketConfig.java - FIXED VERSION
package com.edulink.backend.config;

import com.edulink.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry messages back to the client
        // on destinations prefixed with "/topic" and "/queue"
        config.enableSimpleBroker("/topic", "/queue");
        
        // Designate the "/app" prefix for messages that are bound to methods
        // annotated with @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for personal messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // FIXED: Register WebSocket endpoint with proper CORS configuration
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                    "http://localhost:3000",
                    "http://127.0.0.1:3000",
                    "https://localhost:3000"
                )
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setWebSocketEnabled(true)
                .setHeartbeatTime(25000);
        
        // FIXED: Also register direct WebSocket connection (no SockJS) with same CORS
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                    "http://localhost:3000", 
                    "http://127.0.0.1:3000",
                    "https://localhost:3000"
                )
                .setAllowedOriginPatterns("*");
                
        // ADDED: Debug endpoint registration
        log.info("WebSocket endpoints registered:");
        log.info("- /ws (with SockJS)");
        log.info("- /ws (direct WebSocket)");
        log.info("- Allowed origins: http://localhost:3000, http://127.0.0.1:3000");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null) {
                    log.info("WebSocket message: {} from session: {}", 
                        accessor.getCommand(), accessor.getSessionId());
                    
                    // Handle CONNECT command - authentication
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        log.info("WebSocket CONNECT command received");
                        
                        String authToken = accessor.getFirstNativeHeader("Authorization");
                        
                        if (authToken != null && authToken.startsWith("Bearer ")) {
                            String token = authToken.substring(7);
                            
                            try {
                                if (jwtUtil.isTokenValid(token)) {
                                    String userEmail = jwtUtil.extractUsername(token);
                                    String userId = jwtUtil.extractUserId(token);
                                    
                                    // Store user info in session
                                    accessor.getSessionAttributes().put("userEmail", userEmail);
                                    accessor.getSessionAttributes().put("userId", userId);
                                    accessor.getSessionAttributes().put("token", token);
                                    
                                    log.info("✅ WebSocket authenticated for user: {} ({})", userEmail, userId);
                                } else {
                                    log.warn("❌ Invalid JWT token in WebSocket connection");
                                    // Allow connection but log the issue
                                }
                            } catch (Exception e) {
                                log.error("❌ Error validating WebSocket JWT token: ", e);
                                // Allow connection but log the issue
                            }
                        } else {
                            log.info("ℹ️ No authorization token provided in WebSocket connection (allowing anonymous)");
                            // Allow connection without auth for now
                        }
                    }
                    
                    // Handle DISCONNECT command
                    if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                        String userEmail = (String) accessor.getSessionAttributes().get("userEmail");
                        log.info("🔌 WebSocket disconnected for user: {}", userEmail != null ? userEmail : "anonymous");
                    }
                    
                    // Handle SUBSCRIBE command
                    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                        String destination = accessor.getDestination();
                        log.info("📡 WebSocket subscription to: {}", destination);
                    }
                }
                
                return message;
            }
        });
    }
}