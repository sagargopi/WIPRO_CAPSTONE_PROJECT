package com.example.user.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter){
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // 1. PUBLIC PATHS
                .requestMatchers("/auth/login","/auth/register","/auth/users","/auth/lookup").permitAll()
                .requestMatchers("/loans/*/approve","/loans/*/reject").permitAll()
                .requestMatchers("/api/messages/admin/**", "/api/messages/debug/**", "/api/messages/conversation").permitAll()
                .requestMatchers("/api/messages/unread/**", "/api/messages/send-admin").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/auth/users/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                // 2. CUSTOMER Access + Admin service bridge (for Owner calls)
                .requestMatchers("/investments/**").hasAnyAuthority("USER", "ROLE_USER", "CUSTOMER", "ROLE_CUSTOMER")
                .requestMatchers("/account/**").hasAnyAuthority("USER", "ROLE_USER", "CUSTOMER", "ROLE_CUSTOMER", "ADMIN", "ROLE_ADMIN")

                // 3. NOTIFICATION PATHS
                // Customer/User Notifications
                .requestMatchers("/api/notifications/user/**").hasAnyAuthority("USER", "ROLE_USER", "CUSTOMER", "ROLE_CUSTOMER")
                .requestMatchers("/api/notifications/unread/count/user/**").hasAnyAuthority("USER", "ROLE_USER", "CUSTOMER", "ROLE_CUSTOMER")

                // Admin/Owner Notifications
                .requestMatchers("/api/notifications/owner/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .requestMatchers("/api/notifications/unread/count/owner/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                // Add this line to allow the Feign Client call
                .requestMatchers("/account/balance").hasAnyRole("ADMIN", "CUSTOMER")
                // Inside User Service -> SecurityConfig.java
                .requestMatchers(HttpMethod.PUT, "/auth/users/**").hasAnyRole("ADMIN", "CUSTOMER")
                .requestMatchers("account/auth/users/**").hasAnyRole("ADMIN", "CUSTOMER")

                // Feign Client Bridge (Allow Admin service to send updates)
                .requestMatchers("/api/notifications/loan-update").permitAll() 

                // 4. GENERAL NOTIFICATION CATCH-ALL
                .requestMatchers("/api/notifications/**").hasAnyAuthority("USER", "ADMIN", "ROLE_USER", "ROLE_ADMIN", "CUSTOMER", "ROLE_CUSTOMER")

                // 5. EVERYTHING ELSE
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
