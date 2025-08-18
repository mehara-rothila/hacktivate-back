// File Path: src/main/java/com/edulink/backend/config/SecurityConfig.java
package com.edulink.backend.config;

import com.edulink.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS with our configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Disable CSRF for stateless JWT authentication
            .csrf(csrf -> csrf.disable())
            
            // Configure session management - stateless for JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Handle preflight requests (OPTIONS) - MUST BE FIRST
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Public endpoints - no authentication required
                .requestMatchers("/api/test/**").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register/**").permitAll()
                .requestMatchers("/api/auth/check-email").permitAll()
                .requestMatchers("/api/auth/refresh").permitAll()
                .requestMatchers("/api/files/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/error").permitAll()
                
                // =================== AUTH ENDPOINTS ===================
                .requestMatchers("/api/auth/profile").authenticated()
                .requestMatchers("/api/auth/profile/picture").authenticated()
                .requestMatchers("/api/auth/profile/academic-records").authenticated()
                .requestMatchers("/api/auth/profile/academic-records/**").authenticated()
                .requestMatchers("/api/auth/change-password").authenticated()
                .requestMatchers("/api/auth/logout").authenticated()
                .requestMatchers("/api/auth/status").authenticated()
                
                // =================== USER ENDPOINTS ===================
                .requestMatchers(HttpMethod.GET, "/api/users/lecturers").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/lecturers/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/lecturers/search").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/students").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/department/**").authenticated()
                .requestMatchers("/api/users/**").authenticated()
                
                // =================== APPOINTMENT ENDPOINTS ===================
                .requestMatchers("/api/appointments/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/appointments").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/appointments/{appointmentId}").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/appointments/stats").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/appointments/available-slots").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/appointments").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/appointments/{appointmentId}").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/appointments/{appointmentId}/status").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/appointments/{appointmentId}").authenticated()
                
                // =================== AVAILABILITY ENDPOINTS ===================
                .requestMatchers("/api/availability/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/availability/slots").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/availability/slots/generated").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/availability/stats").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/availability/slots").hasRole("LECTURER")
                .requestMatchers(HttpMethod.POST, "/api/availability/slots/bulk-create").hasRole("LECTURER")
                .requestMatchers(HttpMethod.PUT, "/api/availability/slots/**").hasRole("LECTURER")
                .requestMatchers(HttpMethod.DELETE, "/api/availability/slots/**").hasRole("LECTURER")
                .requestMatchers(HttpMethod.DELETE, "/api/availability/slots/bulk-delete").hasRole("LECTURER")
                
                // =================== QUERY ENDPOINTS ===================
                .requestMatchers("/api/queries/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/queries").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/queries/stats").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/queries").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/queries/{queryId}").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/queries/{queryId}/messages").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/queries/{queryId}/status").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/queries/{queryId}/mark-read").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/queries/mark-all-read").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/queries/{queryId}").authenticated()
                
                // =================== ANNOUNCEMENT ENDPOINTS ===================
                .requestMatchers("/api/announcements/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/announcements").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/announcements/my-created").hasRole("LECTURER")
                .requestMatchers(HttpMethod.POST, "/api/announcements").hasRole("LECTURER")
                .requestMatchers(HttpMethod.PUT, "/api/announcements/{id}/read").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/announcements/{id}/publish").hasRole("LECTURER")
                .requestMatchers(HttpMethod.PUT, "/api/announcements/**").hasRole("LECTURER")
                .requestMatchers(HttpMethod.DELETE, "/api/announcements/**").hasRole("LECTURER")
                
                // =================== COURSE ENDPOINTS ===================
                .requestMatchers("/api/courses/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/courses").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/courses/{courseId}").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("LECTURER")
                .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("LECTURER")
                .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("LECTURER")
                .requestMatchers(HttpMethod.POST, "/api/courses/{courseId}/enroll").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/courses/my-courses/**").authenticated()
                
                // =================== CONVERSATION ENDPOINTS ===================
                .requestMatchers("/api/conversations/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/conversations").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/conversations").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/conversations/{conversationId}/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/conversations/{conversationId}/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/conversations/{conversationId}/**").authenticated()
                
                // =================== RESOURCE ENDPOINTS ===================
                .requestMatchers("/api/resources/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/resources/course/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/resources/upload").hasRole("LECTURER")
                .requestMatchers(HttpMethod.DELETE, "/api/resources/**").hasRole("LECTURER")
                .requestMatchers(HttpMethod.GET, "/api/resources/{resourceId}/download").authenticated()
                
                // =================== ROLE-SPECIFIC ENDPOINTS ===================
                // Student-specific endpoints
                .requestMatchers("/api/student/**").hasRole("STUDENT")
                
                // Lecturer-specific endpoints  
                .requestMatchers("/api/lecturer/**").hasRole("LECTURER")
                
                // Admin-specific endpoints (for future use)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Disable form login (we're using JWT)
            .formLogin(form -> form.disable())
            
            // Disable HTTP Basic authentication
            .httpBasic(basic -> basic.disable())
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration to allow frontend requests
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Get allowed origins from environment variable or use defaults
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        
        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow specific methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allow credentials (important for JWT tokens)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        // Apply CORS to all API endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }

    /**
     * Password encoder bean for hashing passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}