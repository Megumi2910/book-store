package com.second_project.book_store.event.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.second_project.book_store.config.properties.FrontendProperties;
import com.second_project.book_store.entity.ResetPasswordToken;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.event.PasswordResetRequestEvent;
import com.second_project.book_store.service.EmailService;
import com.second_project.book_store.service.ResetPasswordTokenService;

/**
 * Event listener for password reset requests.
 * Creates a reset password token and sends an email with the reset link.
 * 
 * FLOW WITH THYMELEAF:
 * - Email link points to Thymeleaf page: http://localhost:8080/reset-password?token=xxx
 * - Thymeleaf controller displays password reset form (GET /reset-password)
 * - User submits form → POST /reset-password?token=xxx
 * - Thymeleaf controller validates token AND resets password in one step
 * 
 * Note: With Thymeleaf, frontend and backend are on the same server (port 8080).
 * No separate frontend server needed!
 * 
 * Benefits:
 * - Better UX (Thymeleaf handles form display)
 * - More RESTful (single POST operation)
 * - Simpler architecture (one server, no CORS)
 */
@Component
public class PasswordResetRequestEventListener implements ApplicationListener<PasswordResetRequestEvent> {

    private final ResetPasswordTokenService resetPasswordTokenService;
    private final EmailService emailService;
    private final FrontendProperties frontendProperties;

    public PasswordResetRequestEventListener(ResetPasswordTokenService resetPasswordTokenService,
                                            EmailService emailService,
                                            FrontendProperties frontendProperties) {
        this.resetPasswordTokenService = resetPasswordTokenService;
        this.emailService = emailService;
        this.frontendProperties = frontendProperties;
    }

    /**
     * Handle password reset request event asynchronously.
     * 
     * This runs in a separate thread pool so the HTTP request returns immediately
     * without waiting for email to be sent.
     * 
     * @param event The password reset request event
     */
    @Async
    @Override
    public void onApplicationEvent(PasswordResetRequestEvent event) {
        // Get the user from the event
        User user = event.getUser();
        
        // Create reset password token
        ResetPasswordToken token = resetPasswordTokenService.createResetPasswordToken(user);
        
        // Create reset password URL pointing to Thymeleaf page (same server as backend)
        // Thymeleaf controller will extract token from URL and display password reset form
        String frontendBaseUrl = frontendProperties.getBaseUrl();
        String resetPasswordUrl = frontendBaseUrl + "/reset-password?token=" + token.getToken();
        // Example with Thymeleaf: http://localhost:8080/reset-password?token=abc123...
        // (Same server as backend - no separate frontend needed!)

        // Create email information
        String toEmail = user.getEmail();
        String subject = "Reset Your Password";
        String body = "Hello " + user.getFirstName() + ",\n\n"
                    + "You requested to reset your password. Please click the following link to reset your password:\n"
                    + resetPasswordUrl
                    + "\n\nThe link will expire in 15 minutes."
                    + "\n\nIf you did not request this password reset, please ignore this email."
                    + "\n\nBest regards,\nBook Store Team";

        // Send password reset email
        emailService.sendVerificationEmail(toEmail, subject, body);
    }
}

