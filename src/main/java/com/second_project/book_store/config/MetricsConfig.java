package com.second_project.book_store.config;

import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;

/**
 * Configuration for custom business metrics.
 * 
 * Metrics tracked:
 * - User registrations
 * - Verification emails sent
 * - Password reset requests
 * - Login attempts (success/failure)
 * - Email sending duration
 * 
 * Available at: /actuator/metrics
 * 
 * To view specific metric:
 * GET /actuator/metrics/users.registered
 * GET /actuator/metrics/email.verification.sent
 * 
 * Integration with monitoring tools:
 * - Prometheus: Add micrometer-registry-prometheus dependency
 * - Grafana: Create dashboards using Prometheus data
 * - CloudWatch: Add micrometer-registry-cloudwatch dependency
 */
@Configuration
public class MetricsConfig {

    private final MeterRegistry meterRegistry;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Initialize counters (these will be incremented throughout the application)
        
        // User-related metrics
        Counter.builder("users.registered")
               .description("Total number of user registrations")
               .tag("type", "registration")
               .register(meterRegistry);
        
        Counter.builder("users.verified")
               .description("Total number of verified users")
               .tag("type", "verification")
               .register(meterRegistry);
        
        // Email metrics
        Counter.builder("email.verification.sent")
               .description("Total number of verification emails sent")
               .tag("type", "verification")
               .register(meterRegistry);
        
        Counter.builder("email.verification.failed")
               .description("Total number of failed verification emails")
               .tag("type", "verification")
               .register(meterRegistry);
        
        Counter.builder("email.password_reset.sent")
               .description("Total number of password reset emails sent")
               .tag("type", "password_reset")
               .register(meterRegistry);
        
        // Security metrics
        Counter.builder("security.rate_limit.exceeded")
               .description("Number of times rate limit was exceeded")
               .tag("type", "rate_limit")
               .register(meterRegistry);
        
        Counter.builder("security.invalid_token")
               .description("Number of invalid token attempts")
               .tag("type", "security")
               .register(meterRegistry);
        
        // Timer for email sending duration is created automatically when used
        // Just use: Timer.builder("email.send.duration").register(meterRegistry)
    }

    /**
     * Increment user registration counter.
     * Call this when a user successfully registers.
     */
    public void incrementUserRegistrations() {
        meterRegistry.counter("users.registered").increment();
    }

    /**
     * Increment user verification counter.
     * Call this when a user successfully verifies their email.
     */
    public void incrementUserVerifications() {
        meterRegistry.counter("users.verified").increment();
    }

    /**
     * Increment verification email sent counter.
     */
    public void incrementVerificationEmailsSent() {
        meterRegistry.counter("email.verification.sent").increment();
    }

    /**
     * Increment verification email failed counter.
     */
    public void incrementVerificationEmailsFailed() {
        meterRegistry.counter("email.verification.failed").increment();
    }

    /**
     * Increment password reset email sent counter.
     */
    public void incrementPasswordResetEmailsSent() {
        meterRegistry.counter("email.password_reset.sent").increment();
    }

    /**
     * Increment rate limit exceeded counter.
     */
    public void incrementRateLimitExceeded() {
        meterRegistry.counter("security.rate_limit.exceeded").increment();
    }

    /**
     * Increment invalid token counter.
     */
    public void incrementInvalidTokenAttempts() {
        meterRegistry.counter("security.invalid_token").increment();
    }
}

