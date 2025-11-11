package com.second_project.book_store.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.second_project.book_store.event.RegistrationCompleteEvent;
import com.second_project.book_store.service.EmailService;

/**
 * Example of a second listener that can handle email sending independently.
 * This demonstrates the extensibility of the event-driven approach.
 */
@Component
@Order(2) // Executes after token creation (Order 1). Used to send welcome message after the user account is enabled.
public class RegistrationEmailEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationEmailEventListener.class);

    private final EmailService emailService;

    public RegistrationEmailEventListener(EmailService emailService){
        this.emailService = emailService;
    }

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        // Log for now - replace with actual email sending later
        logger.info("TODO: Send welcome email to user: {}", event.getUser().getEmail());
        
        // Future implementation:
        // emailService.sendWelcomeEmail(event.getUser());
    }
}

