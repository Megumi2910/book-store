package com.second_project.book_store.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Type-safe configuration properties for frontend URL settings.
 * 
 * This class binds to properties under 'app.frontend' prefix in application.yml.
 * 
 * Usage in application.yml:
 * app:
 *   frontend:
 *     base-url: ${FRONTEND_BASE_URL:http://localhost:3000}
 * 
 * Purpose:
 * - Used for generating frontend URLs in email links (password reset, email verification)
 * - Allows different frontend URLs for dev/staging/production environments
 */
@Configuration
@ConfigurationProperties(prefix = "app.frontend")
public class FrontendProperties {

    /**
     * Base URL of the frontend application.
     * Default: "http://localhost:3000"
     * 
     * Examples:
     * - Development: "http://localhost:3000"
     * - Staging: "https://staging.example.com"
     * - Production: "https://example.com"
     * 
     * This URL is used to generate links in emails:
     * - Password reset: {baseUrl}/reset-password?token=xxx
     * - Email verification: {baseUrl}/verify-email?token=xxx
     */
    private String baseUrl = "http://localhost:3000";

    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}

