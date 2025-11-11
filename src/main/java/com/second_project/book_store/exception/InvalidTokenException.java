package com.second_project.book_store.exception;

public class InvalidTokenException extends RuntimeException{

    public InvalidTokenException(){
        super("Verification token is invalid");
    }
}
