package com.erp.montfortuganda.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // This makes the external 'uploads' folder visible to your frontend
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/mbsg-auth").setViewName("forward:/mbsg-auth.html");
        registry.addViewController("/dashboard").setViewName("forward:/dashboard.html");
        registry.addViewController("/").setViewName("forward:/login.html");

        String[] spaRoutes = {
                "/superadmin",
                "/superadmin/**",
                "/admin",
                "/admin/**",
                "/parent/**",
                "/schooladmin/**",
                "/academiccoordinator/**",
                "/admissionstaff/**",
                "/feeofficer/**",
                "/teacher/**",
                "/auditor/**"
        };

        for (String route : spaRoutes) {
            registry.addViewController(route)
                    .setViewName("forward:/dashboard.html");
        }

        registry.addViewController("/apply").setViewName("forward:/apply.html");
        registry.addViewController("/apply/status").setViewName("forward:/status.html");
        registry.addViewController("/apply/print").setViewName("forward:/print_application.html");
        registry.addViewController("/apply/print_application").setViewName("forward:/print_application.html");
    }
}
