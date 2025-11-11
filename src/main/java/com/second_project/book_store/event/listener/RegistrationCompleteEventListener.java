package com.second_project.book_store.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.VerificationToken;
import com.second_project.book_store.event.RegistrationCompleteEvent;
import com.second_project.book_store.service.EmailService;
import com.second_project.book_store.service.VerificationTokenService;

@Component
@Order(1) // Executes first to create token
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationCompleteEventListener.class);

    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;

    public RegistrationCompleteEventListener(VerificationTokenService verificationTokenService, EmailService emailService) {
        this.emailService = emailService;
        this.verificationTokenService = verificationTokenService;
    }

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        // Get the user from the event
        User user = event.getUser();
        
        // Create verification token
        VerificationToken token = verificationTokenService.createVerificationToken(user);
        
        // Create verification url
        String verificationUrl = event.getApplicationUrl() + "/verify-registration?token=" + token.getToken();

        // Create email information
        String toEmail = user.getEmail();
        String subject = "Verify Account";
        String body = "Please click the following url to verify your account: " + verificationUrl
                        + "\nThe link will expire in 10 minutes";

        // Send verification email
        emailService.sendVerificationEmail(toEmail, subject, body);

        // Log the verification URL (in production, you'd send this via email)
        logger.info("User registered: {} - Verification URL: {}", user.getEmail(), verificationUrl);
        
    }
}

