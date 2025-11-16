package com.second_project.book_store.model;

import com.second_project.book_store.annotation.ValidEmail;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for forgot password request.
 * User provides their email address to receive a password reset link.
 */
public class ForgotPasswordRequestDto {

    @NotBlank(message = "Email is required")
    @ValidEmail
    private String email;

    public ForgotPasswordRequestDto() {
    }

    public ForgotPasswordRequestDto(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

