package com.second_project.book_store.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.second_project.book_store.annotation.PasswordMatches;

import java.lang.reflect.Method;

/**
 * Validator implementation for @PasswordMatches annotation.
 * Validates that password and matchingPassword fields are equal.
 * 
 * This validator uses reflection to access password fields,
 * making it reusable across different DTO classes.
 */
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordMatchesValidator.class);
    
    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        // Null objects are considered valid (use @NotNull for null checks)
        if (obj == null) {
            return true;
        }
        
        try {
            // Use reflection to get password fields
            String password = getFieldValue(obj, "password");
            String matchingPassword = getFieldValue(obj, "matchingPassword");
            
            // If either password is null, consider invalid
            if (password == null || matchingPassword == null) {
                // Disable default constraint violation and add custom message
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Password and confirmation password are required")
                       .addConstraintViolation();
                return false;
            }
            
            // Check if passwords match
            if (!password.equals(matchingPassword)) {
                // Disable default constraint violation and add custom message
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Passwords do not match. Please ensure both password fields are identical.")
                       .addPropertyNode("matchingPassword") // Associate error with matchingPassword field
                       .addConstraintViolation();
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            // If reflection fails, log and return false
             logger.error("Error validating passwords: {}", e.getMessage(), e);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Error validating password fields")
                   .addConstraintViolation();
            return false;
        }
    }
    
    /**
     * Helper method to get field value using reflection.
     * Tries to find a getter method (e.g., getPassword()) for the field.
     * 
     * @param obj The object to extract value from
     * @param fieldName The field name to extract
     * @return The field value as String
     * @throws Exception if field cannot be accessed
     */
    private String getFieldValue(Object obj, String fieldName) throws Exception {
        // Convert field name to getter method name (e.g., "password" -> "getPassword")
        String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        
        // Get the getter method
        Method getter = obj.getClass().getMethod(getterName);
        
        // Invoke the getter and return the value
        Object value = getter.invoke(obj);
        return value != null ? value.toString() : null;
    }
}
