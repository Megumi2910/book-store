package com.second_project.book_store.exception;

public class UserAlreadyExistedException extends RuntimeException{

    public UserAlreadyExistedException(String message){
        super(message);
    }
}
