package com.second_project.book_store.service;

import com.second_project.book_store.entity.ResetPasswordToken;
import com.second_project.book_store.entity.User;

/**
 * Service interface for managing password reset tokens.
 * Similar to VerificationTokenService but specifically for password reset functionality.
 */
public interface ResetPasswordTokenService {

    /**
     * Creates a new reset password token for the given user.
     * If a token already exists for this user, it will be deleted first.
     * 
     * @param user The user requesting password reset
     * @return The created ResetPasswordToken
     */
    ResetPasswordToken createResetPasswordToken(User user);

    /**
     * Finds a reset password token by its token string.
     * 
     * @param token The token string
     * @return The ResetPasswordToken if found
     * @throws ResetPasswordTokenNotFoundException if token not found
     */
    ResetPasswordToken findByToken(String token);

    /**
     * Finds a reset password token for a specific user.
     * 
     * @param user The user
     * @return The ResetPasswordToken if found
     * @throws ResetPasswordTokenNotFoundException if token not found
     */
    ResetPasswordToken findByUser(User user);

    /**
     * Validates and verifies a reset password token.
     * Checks if token exists, is valid, and not expired.
     * 
     * @param token The token string to verify
     * @throws ResetPasswordTokenNotFoundException if token not found
     * @throws ExpiredTokenException if token is expired or invalid
     */
    void verifyToken(String token);

    /**
     * Deletes a reset password token.
     * 
     * @param token The token to delete
     */
    void deleteToken(ResetPasswordToken token);

    /**
     * Deletes all expired reset password tokens.
     * Can be called periodically via a scheduled task.
     */
    void deleteExpiredTokens();
}

