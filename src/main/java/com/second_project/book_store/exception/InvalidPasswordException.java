package com.second_project.book_store.exception;

/**
 * Exception thrown when password validation fails.
 * Used for:
 * - Current password doesn't match
 * - New password is same as current password
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
    }

    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}

