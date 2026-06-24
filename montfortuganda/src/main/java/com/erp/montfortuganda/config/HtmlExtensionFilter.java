package com.erp.montfortuganda.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HtmlExtensionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // ONLY strip .html if it is NOT an internal component fetch
        // This protects layout.js from crashing!
        if (uri != null && uri.endsWith(".html") && uri.lastIndexOf('/') == 0) {

            String cleanUri = uri.substring(0, uri.length() - 5);

            String queryString = request.getQueryString();
            if (queryString != null) {
                cleanUri += "?" + queryString;
            }

            response.sendRedirect(cleanUri);
            return;
        }

        filterChain.doFilter(request, response);
    }
}