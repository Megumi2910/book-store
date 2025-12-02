package com.second_project.book_store.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for profile update requests.
 * 
 * Note: Email is NOT included as it cannot be changed (used for authentication).
 * Password changes are handled separately via ChangePasswordRequestDto.
 */
public class ProfileUpdateDto {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;

    /**
     * Required field - Vietnamese phone number.
     * Must match Vietnamese phone number format:
     * - Starts with 0 or +84
     * - Followed by valid Vietnamese carrier prefix (3, 5, 7, 8, 9)
     * - Total of 10 digits (excluding country code)
     * Examples: 0912345678, +84912345678, 0387654321
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^(0|\\+84)(3[2-9]|5[6|8|9]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$",
        message = "Phone number must be a valid Vietnamese phone number (e.g., 0912345678 or +84912345678)"
    )
    private String phoneNumber;

    /**
     * Optional field - User address.
     * If provided, must be between 10 and 500 characters.
     */
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    // Constructors
    public ProfileUpdateDto() {
    }

    public ProfileUpdateDto(String firstName, String lastName, String phoneNumber, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
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

    @Override
    public String toString() {
        return "ProfileUpdateDto [firstName=" + firstName + ", lastName=" + lastName + 
               ", phoneNumber=" + phoneNumber + ", address=" + address + "]";
    }
}

