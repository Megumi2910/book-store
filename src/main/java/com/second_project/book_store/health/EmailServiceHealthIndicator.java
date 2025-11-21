package com.second_project.book_store.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;

/**
 * Custom health indicator for email service connectivity.
 * 
 * Checks:
 * - SMTP server connectivity
 * - Mail session availability
 * 
 * Health status:
 * - UP: Email service accessible
 * - DOWN: Email service unreachable
 * 
 * Note: This does NOT send actual emails during health check.
 * It only verifies connection to SMTP server.
 * 
 * Available at: /actuator/health
 */
@Component
public class EmailServiceHealthIndicator implements HealthIndicator {

    private final JavaMailSender mailSender;

    public EmailServiceHealthIndicator(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public Health health() {
        try {
            // Test SMTP connection without sending email
            // Cast to implementation to access testConnection() method
            org.springframework.mail.javamail.JavaMailSenderImpl senderImpl = 
                (org.springframework.mail.javamail.JavaMailSenderImpl) mailSender;
            
            try {
                // This creates a transport connection to verify SMTP server is reachable
                senderImpl.testConnection();
                
                return Health.up()
                        .withDetail("emailService", "SMTP")
                        .withDetail("host", senderImpl.getHost())
                        .withDetail("port", senderImpl.getPort())
                        .withDetail("status", "Connected")
                        .build();
                        
            } catch (MessagingException e) {
                return Health.down()
                        .withDetail("emailService", "SMTP")
                        .withDetail("error", "Connection failed")
                        .withDetail("message", e.getMessage())
                        .build();
            }
            
        } catch (Exception e) {
            return Health.down()
                    .withDetail("emailService", "SMTP")
                    .withDetail("error", e.getClass().getSimpleName())
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}

