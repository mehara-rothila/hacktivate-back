// File Path: src/main/java/com/edulink/backend/config/SecurityConfig.java
package com.edulink.backend.config;

import com.edulink.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless JWT authentication
            .csrf(csrf -> csrf.disable())
            
            // Configure session management - stateless for JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/test/**").permitAll()
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/register/**").permitAll()
                .requestMatchers("/auth/check-email").permitAll()
                .requestMatchers("/auth/refresh").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Protected endpoints requiring authentication
                .requestMatchers("/auth/profile").authenticated()
                .requestMatchers("/auth/change-password").authenticated()
                .requestMatchers("/auth/logout").authenticated()
                .requestMatchers("/auth/status").authenticated()
                
                // Student-specific endpoints
                .requestMatchers("/student/**").hasRole("STUDENT")
                
                // Lecturer-specific endpoints  
                .requestMatchers("/lecturer/**").hasRole("LECTURER")
                
                // Admin-specific endpoints (for future use)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
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
     * Password encoder bean for hashing passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}