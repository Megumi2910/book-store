package com.second_project.book_store.service;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.model.ChangePasswordRequestDto;
import com.second_project.book_store.model.ProfileUpdateDto;
import com.second_project.book_store.model.ResetPasswordRequestDto;
import com.second_project.book_store.model.UserDto;

public interface UserService {

    /**
     * Registers a new user and sends a verification email.
     * Email URL is configured via FrontendProperties (application.yml).
     * 
     * @param userDto User registration data
     * @return The created User entity
     */
    User registerUser(UserDto userDto);
    
    /**
     * Request to send the verification token email to the user via event publisher.
     * Email URL is configured via FrontendProperties (application.yml).
     * Rate limited to prevent abuse (60 seconds between emails).
     * 
     * @param email User's email address
     * @throws UserNotFoundException if user not found
     * @throws UserAlreadyEnabledException if user is already verified
     * @throws RateLimitException if email was sent too recently
     */
    void requestVerificationEmail(String email);

    /**
     * Initiates the password reset process.
     * Publishes an event that will create a reset token and send an email.
     * Email URL is configured via FrontendProperties (application.yml).
     * 
     * @param email The email address of the user requesting password reset
     * @throws UserNotFoundException if user with given email doesn't exist
     */
    void requestPasswordReset(String email);

    /**
     * Resets the user's password using a valid reset token.
     * 
     * @param resetPasswordRequestDto Contains token and new password
     * @throws ResetPasswordTokenNotFoundException if token not found
     * @throws ExpiredTokenException if token is expired or invalid
     */
    void resetPassword(ResetPasswordRequestDto resetPasswordRequestDto);

    /**
     * Changes the password for an authenticated user.
     * Requires the user to provide their current password for verification.
     * 
     * @param userId The ID of the authenticated user
     * @param changePasswordRequestDto Contains current password, new password, and confirmation
     * @throws UserNotFoundException if user not found
     * @throws InvalidPasswordException if current password is incorrect or new password is same as current
     */
    void changePassword(Long userId, ChangePasswordRequestDto changePasswordRequestDto);

    /**
     * Finds a user by email.
     * Used for getting user details from authentication context.
     * 
     * @param email The user's email address
     * @return The User entity
     * @throws UserNotFoundException if user not found
     */
    User findUserByEmail(String email);

    /**
     * Updates the user's profile information.
     * Email cannot be changed as it's used for authentication.
     * 
     * @param userId The ID of the user to update
     * @param profileUpdateDto Contains the updated profile information
     * @return The updated User entity
     * @throws UserNotFoundException if user not found
     * @throws PhoneNumberAlreadyExistedException if phone number is taken by another user
     */
    User updateProfile(Long userId, ProfileUpdateDto profileUpdateDto);

    /**
     * Finds a user by ID.
     * 
     * @param userId The user's ID
     * @return The User entity
     * @throws UserNotFoundException if user not found
     */
    User findUserById(Long userId);
}
