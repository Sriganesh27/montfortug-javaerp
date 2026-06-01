package com.montfort.erp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("-------------------------------------------------");
        System.out.println("JWT DEBUG: Request to " + request.getRequestURI());
        
        final String authHeader = request.getHeader("Authorization");
        System.out.println("JWT DEBUG: Auth Header starts with Bearer? " + (authHeader != null && authHeader.startsWith("Bearer ")));
        
        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("JWT DEBUG: Extracted username: " + username);
            } catch (Exception e) {
                System.out.println("JWT DEBUG: Failed to extract username! Error: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("JWT DEBUG: SecurityContext is empty, loading UserDetails...");
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                System.out.println("JWT DEBUG: UserDetails loaded successfully.");

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    System.out.println("JWT DEBUG: Token is VALID. Setting authentication.");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("JWT DEBUG: Token is INVALID or EXPIRED.");
                }
            } catch (Exception e) {
                System.out.println("JWT DEBUG: Error loading UserDetails: " + e.getMessage());
            }
        } else {
            System.out.println("JWT DEBUG: Username was null OR context already authenticated.");
        }
        
        System.out.println("JWT DEBUG: Continuing filter chain...");
        filterChain.doFilter(request, response);
    }
}
