package com.second_project.book_store.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Type-safe configuration properties for token settings.
 * 
 * This class binds to properties under 'app.security.token' prefix in application.yml.
 * 
 * Usage in application.yml:
 * app:
 *   security:
 *     token:
 *       verification-duration-minutes: 10
 *       reset-password-duration-minutes: 15
 *       rate-limit-seconds: 60
 * 
 * Purpose:
 * - Centralizes token-related configuration
 * - Makes durations configurable per environment
 * - Eliminates magic numbers in code
 */
@Configuration
@ConfigurationProperties(prefix = "app.security.token")
public class TokenProperties {

    /**
     * Verification token expiration duration in minutes.
     * Default: 10 minutes
     * 
     * Used for email verification tokens sent during registration.
     */
    private int verificationDurationMinutes = 10;

    /**
     * Password reset token expiration duration in minutes.
     * Default: 15 minutes
     * 
     * Used for password reset tokens sent during forgot password flow.
     * Longer than verification token since users might need more time.
     */
    private int resetPasswordDurationMinutes = 15;

    /**
     * Rate limit duration in seconds for verification email requests.
     * Default: 60 seconds
     * 
     * Prevents spam by limiting how often users can request verification emails.
     */
    private long rateLimitSeconds = 60;

    // Getters and Setters
    public int getVerificationDurationMinutes() {
        return verificationDurationMinutes;
    }

    public void setVerificationDurationMinutes(int verificationDurationMinutes) {
        this.verificationDurationMinutes = verificationDurationMinutes;
    }

    public int getResetPasswordDurationMinutes() {
        return resetPasswordDurationMinutes;
    }

    public void setResetPasswordDurationMinutes(int resetPasswordDurationMinutes) {
        this.resetPasswordDurationMinutes = resetPasswordDurationMinutes;
    }

    public long getRateLimitSeconds() {
        return rateLimitSeconds;
    }

    public void setRateLimitSeconds(long rateLimitSeconds) {
        this.rateLimitSeconds = rateLimitSeconds;
    }
}

