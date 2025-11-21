package com.second_project.book_store.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.second_project.book_store.service.VerificationTokenService;

/**
 * Scheduled tasks for token cleanup.
 * Automatically removes expired and invalid tokens to prevent database bloat.
 * 
 * BEST PRACTICE: Regular cleanup of expired tokens improves:
 * - Database performance (smaller tables, faster queries)
 * - Security (removes old tokens that could be leaked)
 * - Storage efficiency (reduces unnecessary data)
 */
@Component
public class TokenCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupScheduler.class);
    private final VerificationTokenService verificationTokenService;

    public TokenCleanupScheduler(VerificationTokenService verificationTokenService) {
        this.verificationTokenService = verificationTokenService;
    }

    /**
     * Cleanup expired verification tokens daily at 2:00 AM.
     * 
     * Cron format: "second minute hour day month weekday"
     * - "0 0 2 * * *" = At 2:00 AM every day
     * 
     * Why 2 AM?
     * - Low traffic time for most applications
     * - Minimal impact on user experience
     * - Standard maintenance window
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredVerificationTokens() {
        try {
            verificationTokenService.deleteExpiredToken();
            logger.info("Successfully cleaned up expired verification tokens at {}", 
                java.time.LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Error cleaning up expired verification tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleanup invalid verification tokens daily at 2:05 AM.
     * Runs 5 minutes after expired token cleanup.
     */
    @Scheduled(cron = "0 5 2 * * *")
    public void cleanupInvalidVerificationTokens() {
        try {
            verificationTokenService.deleteInvalidToken();
            logger.info("Successfully cleaned up invalid verification tokens at {}", 
                java.time.LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Error cleaning up invalid verification tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Alternative: Cleanup every 6 hours (uncomment to use instead of daily)
     * This is more aggressive and suitable for high-traffic applications.
     */
    // @Scheduled(fixedRate = 21600000) // 6 hours in milliseconds
    // public void cleanupTokensEvery6Hours() {
    //     try {
    //         verificationTokenService.deleteExpiredToken();
    //         verificationTokenService.deleteInvalidToken();
    //         logger.info("Successfully cleaned up tokens at {}", java.time.LocalDateTime.now());
    //     } catch (Exception e) {
    //         logger.error("Error cleaning up tokens: {}", e.getMessage(), e);
    //     }
    // }
}

