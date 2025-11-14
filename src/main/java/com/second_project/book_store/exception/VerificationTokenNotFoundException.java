package com.second_project.book_store.exception;

public class VerificationTokenNotFoundException extends RuntimeException{

    public VerificationTokenNotFoundException(String message) {
        super(message);
    }
}
