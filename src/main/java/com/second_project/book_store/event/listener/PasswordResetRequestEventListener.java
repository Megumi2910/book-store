package com.second_project.book_store.event.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.second_project.book_store.entity.ResetPasswordToken;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.event.PasswordResetRequestEvent;
import com.second_project.book_store.service.EmailService;
import com.second_project.book_store.service.ResetPasswordTokenService;

/**
 * Event listener for password reset requests.
 * Creates a reset password token and sends an email with the reset link.
 */
@Component
public class PasswordResetRequestEventListener implements ApplicationListener<PasswordResetRequestEvent> {

    private final ResetPasswordTokenService resetPasswordTokenService;
    private final EmailService emailService;

    public PasswordResetRequestEventListener(ResetPasswordTokenService resetPasswordTokenService,
                                            EmailService emailService) {
        this.resetPasswordTokenService = resetPasswordTokenService;
        this.emailService = emailService;
    }

    @Override
    public void onApplicationEvent(PasswordResetRequestEvent event) {
        // Get the user from the event
        User user = event.getUser();
        
        // Create reset password token
        ResetPasswordToken token = resetPasswordTokenService.createResetPasswordToken(user);
        
        // Create reset password URL
        String resetPasswordUrl = event.getApplicationUrl() + "/api/v1/users/reset-password?token=" + token.getToken();

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

