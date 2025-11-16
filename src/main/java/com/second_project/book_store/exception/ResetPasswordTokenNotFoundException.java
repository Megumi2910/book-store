package com.second_project.book_store.exception;

/**
 * Exception thrown when a reset password token is not found.
 */
public class ResetPasswordTokenNotFoundException extends RuntimeException {

    public ResetPasswordTokenNotFoundException(String message) {
        super(message);
    }

    public ResetPasswordTokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

