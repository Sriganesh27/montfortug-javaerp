package com.erp.montfortuganda.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Core Pages
        registry.addViewController("/").setViewName("forward:/login.html");
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/dashboard").setViewName("forward:/dashboard.html");
        registry.addViewController("/apply").setViewName("forward:/apply.html");
        registry.addViewController("/apply/status").setViewName("forward:/status.html");
        registry.addViewController("/apply/print").setViewName("forward:/print_application.html");

        // --- SECURE SPA ROUTING ---
        // We explicitly map our roles so we don't accidentally intercept the /css/ or /js/ folders!
        String[] spaRoutes = {
                "/superadmin/**",
                "/parent/**",
                "/schooladmin/**",
                "/academiccoordinator/**",
                "/admissionstaff/**",
                "/feeofficer/**",
                "/teacher/**",
                "/auditor/**"
        };

        for (String route : spaRoutes) {
            registry.addViewController(route).setViewName("forward:/dashboard.html");
        }
    }
}