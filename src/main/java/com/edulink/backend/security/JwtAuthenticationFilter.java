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
            // Extract JWT token from request
            String jwt = extractTokenFromRequest(request);
            
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(jwt, request);
            }
            
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Continue with the filter chain even if authentication fails
            // The security configuration will handle unauthorized access
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }
        
        return null;
    }

    /**
     * Authenticate user using JWT token
     */
    private void authenticateUser(String jwt, HttpServletRequest request) {
        try {
            // Validate token first
            if (!jwtUtil.isTokenValid(jwt)) {
                log.debug("Invalid JWT token");
                return;
            }

            // Extract user information from token
            String userEmail = jwtUtil.extractUsername(jwt);
            String userId = jwtUtil.extractUserId(jwt);
            String role = jwtUtil.extractRole(jwt);

            if (userEmail == null || userId == null || role == null) {
                log.debug("Invalid token claims");
                return;
            }

            // Additional validation against database
            Optional<User> userOptional = userService.findByEmail(userEmail);
            if (userOptional.isEmpty()) {
                log.debug("User not found in database: {}", userEmail);
                return;
            }

            User user = userOptional.get();

            // Check if user is active
            if (!user.isActive()) {
                log.debug("User account is inactive: {}", userEmail);
                return;
            }

            // Validate token against user
            if (!jwtUtil.validateToken(jwt, userEmail)) {
                log.debug("JWT token validation failed for user: {}", userEmail);
                return;
            }

            // Create authentication token
            UsernamePasswordAuthenticationToken authToken = createAuthenticationToken(user, request);
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            log.debug("Successfully authenticated user: {} with role: {}", userEmail, role);

        } catch (Exception e) {
            log.error("Error during JWT authentication: {}", e.getMessage());
        }
    }

    /**
     * Create Spring Security authentication token
     */
    private UsernamePasswordAuthenticationToken createAuthenticationToken(User user, HttpServletRequest request) {
        // Create authority based on user role
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().toString());
        
        // Create authentication token
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            user.getEmail(), // Principal (username)
            null, // Credentials (we don't store password in token)
            Collections.singletonList(authority) // Authorities
        );
        
        // Set additional details
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        return authToken;
    }

    /**
     * Skip JWT filter for certain endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Skip JWT validation for these paths
        return path.startsWith("/api/test/") ||
               path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/check-email") ||
               path.startsWith("/api/actuator/") ||
               path.equals("/api/auth/refresh") ||
               path.contains("/error");
    }
}