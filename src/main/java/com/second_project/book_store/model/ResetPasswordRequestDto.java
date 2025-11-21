package com.second_project.book_store.model;

import com.second_project.book_store.annotation.PasswordMatches;
import com.second_project.book_store.annotation.StrongPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for password reset request.
 * User provides the reset token and new password.
 * 
 * Flow:
 * 1. User requests password reset (ForgotPasswordRequestDto)
 * 2. User receives email with reset link containing token
 * 3. User clicks link and submits this DTO with new password
 */
@PasswordMatches
public class ResetPasswordRequestDto {

    // Token can be provided in query parameter, so it's optional in body
    // Validation will be handled in controller if both are missing
    private String token;

    @NotBlank(message = "Password is required")
    @StrongPassword
    @Size(max = 50, message = "Password must not exceed 50 characters")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String matchingPassword;

    public ResetPasswordRequestDto() {
    }

    public ResetPasswordRequestDto(String token, String password, String matchingPassword) {
        this.token = token;
        this.password = password;
        this.matchingPassword = matchingPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMatchingPassword() {
        return matchingPassword;
    }

    public void setMatchingPassword(String matchingPassword) {
        this.matchingPassword = matchingPassword;
    }
}

