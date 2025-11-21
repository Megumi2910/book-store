package com.second_project.book_store.model;

import com.second_project.book_store.annotation.PasswordMatches;
import com.second_project.book_store.annotation.StrongPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for change password request.
 * Used when user is logged in and wants to change their password.
 * 
 * Flow:
 * 1. User is authenticated (logged in)
 * 2. User provides current password + new password
 * 3. System verifies current password matches
 * 4. System updates to new password
 */
@PasswordMatches
public class ChangePasswordRequestDto {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @StrongPassword
    @Size(max = 50, message = "Password must not exceed 50 characters")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String matchingPassword;

    public ChangePasswordRequestDto() {
    }

    public ChangePasswordRequestDto(String currentPassword, String password, String matchingPassword) {
        this.currentPassword = currentPassword;
        this.password = password;
        this.matchingPassword = matchingPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
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

