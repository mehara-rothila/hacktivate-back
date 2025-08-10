// File Path: src/main/java/com/edulink/backend/security/JwtAuthenticationFilter.java
package com.edulink.backend.security;

import com.edulink.backend.service.UserService;
import com.edulink.backend.util.JwtUtil;
import com.edulink.backend.model.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractTokenFromRequest(request);
            
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(jwt, request);
            }
            
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }
        
        return null;
    }

    private void authenticateUser(String jwt, HttpServletRequest request) {
        try {
            if (!jwtUtil.isTokenValid(jwt)) {
                log.debug("Invalid JWT token");
                return;
            }

            String userEmail = jwtUtil.extractUsername(jwt);
            if (userEmail == null) {
                log.debug("Invalid token claims: email is null");
                return;
            }

            Optional<User> userOptional = userService.findByEmail(userEmail);
            if (userOptional.isEmpty()) {
                log.debug("User not found in database: {}", userEmail);
                return;
            }

            User user = userOptional.get();
            if (!user.isActive()) {
                log.debug("User account is inactive: {}", userEmail);
                return;
            }

            if (!jwtUtil.validateToken(jwt, userEmail)) {
                log.debug("JWT token validation failed for user: {}", userEmail);
                return;
            }

            UsernamePasswordAuthenticationToken authToken = createAuthenticationToken(user, request);
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Successfully authenticated user: {} with role: {}", userEmail, user.getRole());

        } catch (Exception e) {
            log.error("Error during JWT authentication: {}", e.getMessage());
        }
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(User user, HttpServletRequest request) {
        // CORRECTED: Replaced 'in' with '+' for Java string concatenation
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().toString());
        
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            user.getEmail(),
            null,
            Collections.singletonList(authority)
        );
        
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        return authToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/test/") ||
               path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/check-email") ||
               path.startsWith("/api/actuator/") ||
               path.equals("/api/auth/refresh") ||
               path.contains("/error");
    }
}