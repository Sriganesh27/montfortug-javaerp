package com.erp.montfortuganda.auth.jwt;

import com.erp.montfortuganda.auth.service.UserDetailsServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    JwtAuthenticationFilter.class
            );

    private static final String JWT_COOKIE_NAME =
            "jwt_token";

    private static final String BEARER_PREFIX =
            "Bearer ";

    private static final Set<String> FILTER_EXCLUDED_PATHS =
            Set.of(
                    "/api/auth/login",
                    "/api/auth/logout",
                    "/api/auth/change-temporary-password"
            );

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsServiceImpl userDetailsService
    ) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(
            @NonNull HttpServletRequest request
    ) {
        String servletPath = request.getServletPath();

        return !servletPath.startsWith("/api/")
                || servletPath.startsWith("/api/public/")
                || FILTER_EXCLUDED_PATHS.contains(servletPath);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String accessToken =
                resolveAccessToken(request);

        if (
                accessToken != null
                        && SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null
        ) {
            authenticateRequest(
                    accessToken,
                    request
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private String resolveAccessToken(
            HttpServletRequest request
    ) {
        String cookieToken =
                extractCookieToken(request);

        if (isAccessToken(cookieToken)) {
            return cookieToken;
        }

        String headerToken =
                extractBearerToken(request);

        if (isAccessToken(headerToken)) {
            return headerToken;
        }

        return null;
    }

    private String extractCookieToken(
            HttpServletRequest request
    ) {
        Cookie[] cookies =
                request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (
                    JWT_COOKIE_NAME.equals(
                            cookie.getName()
                    )
            ) {
                return trimToNull(
                        cookie.getValue()
                );
            }
        }

        return null;
    }

    private String extractBearerToken(
            HttpServletRequest request
    ) {
        String authorizationHeader =
                request.getHeader(
                        HttpHeaders.AUTHORIZATION
                );

        if (
                authorizationHeader == null
                        || !authorizationHeader.startsWith(
                        BEARER_PREFIX
                )
        ) {
            return null;
        }

        return trimToNull(
                authorizationHeader.substring(
                        BEARER_PREFIX.length()
                )
        );
    }

    private boolean isAccessToken(
            String token
    ) {
        if (token == null) {
            return false;
        }

        try {
            return jwtUtil.extractTokenType(token)
                    == JwtUtil.TokenType.ACCESS;
        } catch (
                JwtException
                | IllegalArgumentException exception
        ) {
            LOGGER.debug(
                    "Ignoring an invalid JWT access token."
            );

            return false;
        }
    }

    private void authenticateRequest(
            String accessToken,
            HttpServletRequest request
    ) {
        try {
            String username =
                    jwtUtil.extractUsername(
                            accessToken
                    );

            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(
                                    username
                            );

            if (
                    !jwtUtil.validateAccessToken(
                            accessToken,
                            userDetails
                    )
            ) {
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

        } catch (
                JwtException
                | UsernameNotFoundException
                | IllegalArgumentException exception
        ) {
            LOGGER.debug(
                    "JWT authentication failed."
            );
        }
    }

    private String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmedValue =
                value.trim();

        return trimmedValue.isEmpty()
                ? null
                : trimmedValue;
    }
}
