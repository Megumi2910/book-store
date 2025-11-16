package com.second_project.book_store.event;

import org.springframework.context.ApplicationEvent;

import com.second_project.book_store.entity.User;

/**
 * Event published when a user requests a password reset.
 * Listeners will handle token creation and email sending.
 */
public class PasswordResetRequestEvent extends ApplicationEvent {

    private final User user;
    private final String applicationUrl;

    public PasswordResetRequestEvent(User user, String applicationUrl) {
        super(user);
        this.user = user;
        this.applicationUrl = applicationUrl;
    }

    public User getUser() {
        return user;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }
}

