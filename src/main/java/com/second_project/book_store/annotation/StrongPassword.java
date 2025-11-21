package com.second_project.book_store.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Custom validation annotation for strong password requirements.
 * 
 * Password must contain:
 * - At least 8 characters
 * - At least one uppercase letter (A-Z)
 * - At least one lowercase letter (a-z)
 * - At least one digit (0-9)
 * - At least one special character (@$!%*?&)
 * 
 * Usage example:
 * @StrongPassword
 * private String password;
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = com.second_project.book_store.validator.StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
    String message() default "Password must be at least 8 characters and contain uppercase, lowercase, digit, and special character (@$!%*?&)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

