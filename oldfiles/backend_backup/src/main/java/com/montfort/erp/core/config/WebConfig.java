package com.montfort.erp.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map multiple potential upload paths to ensure compatibility with PHP-generated paths
        String uploadDir = Paths.get(System.getProperty("user.dir"), "public", "assets", "uploads").toUri().toString();
        
        registry.addResourceHandler("/assets/uploads/**", "/uploads/**", "/applications/**")
                .addResourceLocations(
                        uploadDir + "/", 
                        "classpath:/static/assets/uploads/", 
                        "classpath:/static/uploads/",
                        "classpath:/static/assets/uploads/applications/"
                );
    }
}

