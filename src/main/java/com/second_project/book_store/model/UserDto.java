package com.second_project.book_store.model;

import com.second_project.book_store.annotation.PasswordMatches;
import com.second_project.book_store.annotation.StrongPassword;
import com.second_project.book_store.annotation.ValidEmail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for User registration/update.
 * Uses custom validation annotations to ensure data integrity.
 */
@PasswordMatches // Applied at class level to compare password fields
public class UserDto {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @ValidEmail // Custom validation for email format
    private String email;

    /**
     * Optional field - Vietnamese phone number.
     * If provided, must match Vietnamese phone number format:
     * - Starts with 0 or +84
     * - Followed by valid Vietnamese carrier prefix (3, 5, 7, 8, 9)
     * - Total of 10 digits (excluding country code)
     * Examples: 0912345678, +84912345678, 0387654321
     */
    @Pattern(
        regexp = "^(0|\\+84)(3[2-9]|5[6|8|9]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$",
        message = "Phone number must be a valid Vietnamese phone number (e.g., 0912345678 or +84912345678)"
    )
    private String phoneNumber;

    /**
     * Optional field - User address.
     * If provided, must not exceed 500 characters.
     */
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotBlank(message = "Password is required")
    @StrongPassword
    @Size(max = 50, message = "Password must not exceed 50 characters")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String matchingPassword; // Must match the password field

    // Constructors
    public UserDto() {
    }

    public UserDto(String firstName, String lastName, String email, String phoneNumber, String address, 
                   String password, String matchingPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.password = password;
        this.matchingPassword = matchingPassword;
    }

    public UserDto(String firstName, String lastName, String email, String phoneNumber, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    @Override
    public String toString() {
        return "UserDto [firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", phoneNumber=" + phoneNumber 
                + ", address=" + address + "]";
    }
}

