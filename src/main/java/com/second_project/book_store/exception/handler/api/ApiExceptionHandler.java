package com.second_project.book_store.exception.handler.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.second_project.book_store.exception.ExpiredTokenException;
import com.second_project.book_store.exception.InvalidPasswordException;
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

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPasswordException(InvalidPasswordException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "invalid_password", "message", exception.getMessage()));
    }

    /**
     * Handles validation errors from @Valid annotations.
     * Returns detailed field-level and class-level error messages for better user experience.
     * 
     * Handles both:
     * - Field-level validators (e.g., @NotBlank, @Size) → FieldError
     * - Class-level validators (e.g., @PasswordMatches) → ObjectError
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            if (error instanceof FieldError) {
                // Field-level validation error (e.g., @NotBlank, @Size)
                FieldError fieldError = (FieldError) error;
                String fieldName = fieldError.getField();
                String errorMessage = fieldError.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            } else if (error instanceof ObjectError) {
                // Class-level validation error (e.g., @PasswordMatches)
                ObjectError objectError = (ObjectError) error;
                String errorMessage = objectError.getDefaultMessage();
                
                // For @PasswordMatches, associate error with matchingPassword field for better UX
                // This makes it clear which field has the issue
                if (errorMessage != null && errorMessage.toLowerCase().contains("password")) {
                    errors.put("matchingPassword", errorMessage);
                } else {
                    // Generic class-level error
                    errors.put("_class", errorMessage);
                }
            }
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "validation_failed");
        response.put("message", "Validation failed. Please check the following fields:");
        response.put("errors", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
