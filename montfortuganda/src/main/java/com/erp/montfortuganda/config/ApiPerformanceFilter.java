package com.erp.montfortuganda.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Logs slow API requests without changing existing request or response
 * behavior.
 */
@Component
public class ApiPerformanceFilter extends OncePerRequestFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ApiPerformanceFilter.class
            );

    private static final long SLOW_REQUEST_THRESHOLD_MS =
            1_000L;

    private static final int MAXIMUM_URI_LENGTH =
            500;

    @Override
    protected boolean shouldNotFilter(
            @NonNull HttpServletRequest request
    ) {
        String requestUri =
                request.getRequestURI();

        return !requestUri.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        long startedAt =
                System.nanoTime();

        try {
            filterChain.doFilter(
                    request,
                    response
            );
        } finally {
            long durationMs =
                    TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - startedAt
                    );

            if (
                    durationMs
                            >= SLOW_REQUEST_THRESHOLD_MS
            ) {
                LOGGER.warn(
                        "SLOW API: {} {} status={} duration={}ms",
                        request.getMethod(),
                        sanitizeRequestUri(
                                request.getRequestURI()
                        ),
                        response.getStatus(),
                        durationMs
                );
            }
        }
    }

    private String sanitizeRequestUri(
            String requestUri
    ) {
        String sanitized =
                requestUri.replaceAll(
                        "[\\r\\n\\t]",
                        "_"
                );

        if (
                sanitized.length()
                        <= MAXIMUM_URI_LENGTH
        ) {
            return sanitized;
        }

        return sanitized.substring(
                0,
                MAXIMUM_URI_LENGTH
        );
    }
}