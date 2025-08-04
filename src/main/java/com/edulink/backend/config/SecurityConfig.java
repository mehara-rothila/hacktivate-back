package com.edulink.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API endpoints
            .csrf(csrf -> csrf.disable())
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Allow public access to test endpoints
                .requestMatchers("/test/**").permitAll()
                
                // Allow public access to actuator health endpoint
                .requestMatchers("/actuator/health").permitAll()
                
                // Allow public access to error pages
                .requestMatchers("/error").permitAll()
                
                // All other requests need authentication (for later)
                .anyRequest().authenticated()
            )
            
            // For now, disable form login (we'll add JWT later)
            .formLogin(form -> form.disable())
            
            // Disable HTTP Basic authentication
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}