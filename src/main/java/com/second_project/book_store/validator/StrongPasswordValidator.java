package com.second_project.book_store.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.second_project.book_store.annotation.StrongPassword;

import java.util.regex.Pattern;

/**
 * Validator implementation for @StrongPassword annotation.
 * Validates password strength based on complexity requirements.
 * 
 * Requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one digit
 * - At least one special character (@$!%*?&)
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    
    // Regex pattern for strong password validation
    // (?=.*[a-z]) - at least one lowercase letter
    // (?=.*[A-Z]) - at least one uppercase letter
    // (?=.*\d) - at least one digit
    // (?=.*[@$!%*?&]) - at least one special character
    // [A-Za-z\d@$!%*?&]{8,} - allowed characters, minimum 8 length
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    
    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Null or empty passwords should be caught by @NotBlank
        if (password == null || password.isEmpty()) {
            return true; // Let @NotBlank handle this
        }
        
        // Check if password matches the pattern
        boolean isValid = pattern.matcher(password).matches();
        
        if (!isValid) {
            // Provide detailed error message
            context.disableDefaultConstraintViolation();
            
            StringBuilder errorMessage = new StringBuilder("Password must contain: ");
            boolean hasError = false;
            
            if (!password.matches(".*[a-z].*")) {
                errorMessage.append("at least one lowercase letter");
                hasError = true;
            }
            
            if (!password.matches(".*[A-Z].*")) {
                if (hasError) errorMessage.append(", ");
                errorMessage.append("at least one uppercase letter");
                hasError = true;
            }
            
            if (!password.matches(".*\\d.*")) {
                if (hasError) errorMessage.append(", ");
                errorMessage.append("at least one digit");
                hasError = true;
            }
            
            if (!password.matches(".*[@$!%*?&].*")) {
                if (hasError) errorMessage.append(", ");
                errorMessage.append("at least one special character (@$!%*?&)");
                hasError = true;
            }
            
            if (password.length() < 8) {
                if (hasError) errorMessage.append(", ");
                errorMessage.append("minimum 8 characters");
            }
            
            context.buildConstraintViolationWithTemplate(errorMessage.toString())
                   .addConstraintViolation();
        }
        
        return isValid;
    }
}

