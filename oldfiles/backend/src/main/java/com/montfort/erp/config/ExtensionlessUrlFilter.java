package com.montfort.erp.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ExtensionlessUrlFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String uri = req.getRequestURI();

        // Skip APIs, static folders, and files that already have an extension (like .css, .js)
        if (uri.startsWith("/api") || uri.startsWith("/assets") || uri.startsWith("/views") 
            || uri.contains(".") || uri.equals("/")) {
            chain.doFilter(request, response);
            return;
        }

        // 1. Root Level Pages (e.g. /login -> /login.html)
        if (uri.lastIndexOf("/") == 0) {
            req.getRequestDispatcher(uri + ".html").forward(request, response);
            return;
        }

        // 2. Module Level Pages (e.g. /superadmin/dashboard -> /views/superadmin/dashboard.html)
        req.getRequestDispatcher("/views" + uri + ".html").forward(request, response);
    }
}
