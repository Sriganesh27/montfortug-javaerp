package com.erp.montfortuganda.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Loads login page when visiting http://localhost:8080/
        registry.addViewController("/").setViewName("forward:/login.html");

        // Loads login page when visiting http://localhost:8080/login
        registry.addViewController("/login").setViewName("forward:/login.html");

        // Loads dashboard when visiting http://localhost:8080/superadmin
        registry.addViewController("/superadmin").setViewName("forward:/superadmin.html");
    }
}