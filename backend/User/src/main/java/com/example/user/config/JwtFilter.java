package com.example.user.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.user.service.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // 1. Skip only public auth endpoints
        if (path.equals("/auth/login") || path.equals("/auth/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Inside com.example.user.config.JwtFilter

            try {
                String username = jwtService.extractUserName(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtService.validateToken(token, username)) {
                        
                        // 1. EXTRACT THE ROLE FROM THE TOKEN
                        String role = jwtService.extractRole(token);
                        
                        // 2. LOG IT (This will show up in your IntelliJ/VS Code terminal)
                        System.out.println("🔍 Filter found User: " + username + " with Role: " + role);

                        // 3. CREATE THE AUTHORITY
                        // We use both with and without ROLE_ prefix to be 100% safe
                        List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority(role),
                            new SimpleGrantedAuthority("ROLE_" + role)
                        );

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ JWT Auth Error: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
} // <--- THIS WAS THE MISSING BRACKET
