package com.tandiantong.bootstrap.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 跨域访问配置，仅允许明确配置的后台前端来源。 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfiguration(@Value("${TDT_CORS_ALLOWED_ORIGINS:http://localhost:5173}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Trace-Id")
                .exposedHeaders("X-Trace-Id", "Content-Disposition")
                .maxAge(3600);
    }
}
