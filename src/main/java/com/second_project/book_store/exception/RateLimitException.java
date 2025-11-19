package com.second_project.book_store.exception;

/**
 * Exception thrown when a rate limit is exceeded.
 * Used to prevent spam/abuse of features like email sending.
 */
public class RateLimitException extends RuntimeException {
    
    private final long secondsRemaining;
    
    public RateLimitException(String message, long secondsRemaining) {
        super(message);
        this.secondsRemaining = secondsRemaining;
    }
    
    public long getSecondsRemaining() {
        return secondsRemaining;
    }
}

