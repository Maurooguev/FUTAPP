package com.futapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import java.nio.file.Path;

@Configuration
public class WebConfig {
    private final String[] allowedOrigins;
    private final String uploadsLocation;

    public WebConfig(@Value("${futapp.cors.allowed-origins:http://localhost:5173}") String allowedOrigins,
                     @Value("${futapp.uploads.directory:./data/uploads}") String uploadsDirectory) {
        this.allowedOrigins = allowedOrigins.split("\\s*,\\s*");
        this.uploadsLocation = Path.of(uploadsDirectory).toAbsolutePath().normalize().toUri().toString();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**").addResourceLocations(uploadsLocation);
            }
        };
    }
}
