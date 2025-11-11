package com.second_project.book_store.exception.handler.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.second_project.book_store.exception.ExpiredTokenException;
import com.second_project.book_store.exception.InvalidTokenException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<Map<String, String>> handleExpiredTokenException (ExpiredTokenException exception){

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "expired", "message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTokenException (InvalidTokenException exception){

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "invalid", "message", exception.getMessage()));
    }
    
}
