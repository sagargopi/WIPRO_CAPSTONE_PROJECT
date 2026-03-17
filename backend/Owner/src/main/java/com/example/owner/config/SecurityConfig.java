package com.example.owner.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. Public Endpoints (Login, Register, and Service-to-Service alerts)
                .requestMatchers("/owner/login", "/owner/register").permitAll()
                .requestMatchers("/loan-approvals/create", "/api/notifications/balance-alert").permitAll()
                
                // 2. Admin/Owner Specific Access
                // We check for both "ADMIN" and "ROLE_ADMIN" to be safe across microservices
                .requestMatchers("/api/notifications/owner/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .requestMatchers("/api/notifications/send").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .requestMatchers("/owner/users/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                // File: SecurityConfig.java in User Service
               
                .requestMatchers("/loan-approvals/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN") // Covers /pending, /approve, /reject
                
                // 3. Any other request must be authenticated
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            // The JWT Filter is critical; it must be added before the standard auth filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Ensure your frontend URL matches exactly
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        
        // Standard Banking App Methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Crucial: Authorization header must be allowed for JWT to pass
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        
        // Allow cookies/auth headers to be sent
        configuration.setAllowCredentials(true);
        
        // Cache CORS response for 1 hour to improve performance
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}