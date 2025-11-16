package com.second_project.book_store.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Type-safe configuration properties for CORS (Cross-Origin Resource Sharing) settings.
 * 
 * This class binds to properties under 'app.cors' prefix in application.yml.
 * 
 * Usage in application.yml:
 * app:
 *   cors:
 *     allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:8080}
 */
@Configuration
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * Comma-separated list of allowed origins for CORS.
     * Default: "http://localhost:3000,http://localhost:8080"
     * 
     * Examples:
     * - Single origin: "http://localhost:3000"
     * - Multiple origins: "http://localhost:3000,http://localhost:8080,https://example.com"
     * - All origins: "*" (not recommended for production)
     */
    private String allowedOrigins = "http://localhost:3000,http://localhost:8080";

    // Getters and Setters
    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}

