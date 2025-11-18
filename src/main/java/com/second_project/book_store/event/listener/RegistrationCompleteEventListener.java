package com.second_project.book_store.event.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.second_project.book_store.config.properties.FrontendProperties;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.VerificationToken;
import com.second_project.book_store.event.RegistrationCompleteEvent;
import com.second_project.book_store.service.EmailService;
import com.second_project.book_store.service.VerificationTokenService;

/**
 * Event listener for user registration completion.
 * Creates a verification token and sends an email verification link.
 * 
 * FLOW WITH THYMELEAF:
 * - Email link points to backend API: http://localhost:8080/api/v1/users/verify-registration?token=xxx
 * - API endpoint verifies the token and enables the user account
 * 
 * Note: With Thymeleaf, frontend and backend are on the same server (port 8080).
 * Email URL is configured via FrontendProperties (application.yml).
 */
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;
    private final FrontendProperties frontendProperties;

    public RegistrationCompleteEventListener(VerificationTokenService verificationTokenService, 
                                            EmailService emailService,
                                            FrontendProperties frontendProperties) {
        this.emailService = emailService;
        this.verificationTokenService = verificationTokenService;
        this.frontendProperties = frontendProperties;
    }

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        // Get the user from the event
        User user = event.getUser();
        
        // Create verification token
        VerificationToken token = verificationTokenService.createVerificationToken(user);
        
        // Create verification URL using configured frontend base URL
        // With Thymeleaf: frontend and backend are on same server (e.g., http://127.0.0.1:8080)
        String frontendBaseUrl = frontendProperties.getBaseUrl();
        String verificationUrl = frontendBaseUrl + "/verify-registration?token=" + token.getToken();

        // Create email information
        String toEmail = user.getEmail();
        String subject = "Verify Account";
        String body = "Hello " + user.getFirstName() + ",\n\n"
                    + "Thank you for registering! Please click the following link to verify your account:\n"
                    + verificationUrl
                    + "\n\nThe link will expire in 10 minutes."
                    + "\n\nIf you did not create this account, please ignore this email."
                    + "\n\nBest regards,\nBook Store Team";

        // Send verification email
        emailService.sendVerificationEmail(toEmail, subject, body);
    }
}

