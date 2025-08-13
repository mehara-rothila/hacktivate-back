// File Path: src/main/java/com/edulink/backend/config/JwtChannelInterceptor.java
package com.edulink.backend.config;

import com.edulink.backend.model.entity.User;
import com.edulink.backend.service.UserService;
import com.edulink.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            log.debug("Authorization header: {}", authorization);

            if (authorization != null && !authorization.isEmpty()) {
                String authHeader = authorization.get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String jwt = authHeader.substring(7);
                    try {
                        if (jwtUtil.isTokenValid(jwt)) {
                            String userEmail = jwtUtil.extractUsername(jwt);
                            Optional<User> userOptional = userService.findByEmail(userEmail);

                            if (userOptional.isPresent()) {
                                User user = userOptional.get();
                                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().toString());
                                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    user.getEmail(),
                                    null,
                                    Collections.singletonList(authority)
                                );
                                SecurityContextHolder.getContext().setAuthentication(authToken);
                                accessor.setUser(authToken); // Associate user with the WebSocket session
                                log.info("Authenticated WebSocket user: {}", userEmail);
                            }
                        }
                    } catch (Exception e) {
                        log.error("WebSocket authentication error: {}", e.getMessage());
                    }
                }
            }
        }
        return message;
    }
}
