package com.second_project.book_store.exception;

public class PhoneNumberAlreadyExistedException extends RuntimeException{
    public PhoneNumberAlreadyExistedException(String message){
        super(message);
    }
}
