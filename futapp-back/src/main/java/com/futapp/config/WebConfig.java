package com.futapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuditoriaInterceptor auditoria;
    private final String[] allowedOrigins;
    private final String uploadsLocation;

    public WebConfig(AuditoriaInterceptor auditoria,
                     @Value("${futapp.cors.allowed-origins:http://localhost:5173}") String allowedOrigins,
                     @Value("${futapp.uploads.directory:./data/uploads}") String uploadsDirectory) {
        this.auditoria = auditoria;
        this.allowedOrigins = allowedOrigins.split("\\s*,\\s*");
        this.uploadsLocation = Path.of(uploadsDirectory).toAbsolutePath().normalize().toUri().toString();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditoria);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Request-ID")
                .exposedHeaders("X-Request-ID", "X-RateLimit-Limit", "X-RateLimit-Remaining", "Retry-After")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**").addResourceLocations(uploadsLocation);
    }
}
