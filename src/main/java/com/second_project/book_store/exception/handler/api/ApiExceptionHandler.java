package com.second_project.book_store.exception.handler.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.second_project.book_store.exception.ExpiredTokenException;
import com.second_project.book_store.exception.ResetPasswordTokenNotFoundException;
import com.second_project.book_store.exception.UserAlreadyEnabledException;
import com.second_project.book_store.exception.UserNotFoundException;
import com.second_project.book_store.exception.VerificationTokenNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<Map<String, String>> handleExpiredTokenException (ExpiredTokenException exception){

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "expired", "message", exception.getMessage()));
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException (UserNotFoundException exception){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "not found", "message", exception.getMessage()));
    }
    
    @ExceptionHandler(VerificationTokenNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleVerificationTokenNotFoundException (VerificationTokenNotFoundException exception){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "not found", "message", exception.getMessage()));
    }

    @ExceptionHandler(UserAlreadyEnabledException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyEnabledException (UserAlreadyEnabledException exception){

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "already verified", "message", exception.getMessage()));
    }

    @ExceptionHandler(ResetPasswordTokenNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResetPasswordTokenNotFoundException (ResetPasswordTokenNotFoundException exception){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "not found", "message", exception.getMessage()));
    }

}
