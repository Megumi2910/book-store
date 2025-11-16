package com.second_project.book_store.service;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.model.ChangePasswordRequestDto;
import com.second_project.book_store.model.ResetPasswordRequestDto;
import com.second_project.book_store.model.UserDto;

public interface UserService {

    User registerUser(UserDto userDto, String applicationUrl);
    
    void resendVerificationToken(String email, String applicationUrl);

    /**
     * Initiates the password reset process.
     * Publishes an event that will create a reset token and send an email.
     * 
     * @param email The email address of the user requesting password reset
     * @param applicationUrl The base URL of the application (for constructing reset link)
     * @throws UserNotFoundException if user with given email doesn't exist
     */
    void requestPasswordReset(String email, String applicationUrl);

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
}
