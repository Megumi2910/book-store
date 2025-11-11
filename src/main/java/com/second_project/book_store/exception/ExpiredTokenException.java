package com.second_project.book_store.exception;
 
public class ExpiredTokenException extends RuntimeException{
    
    public ExpiredTokenException(){
        super("Verification token has expired");
    }
}
