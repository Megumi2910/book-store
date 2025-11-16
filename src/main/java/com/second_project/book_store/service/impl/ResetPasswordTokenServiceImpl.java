package com.second_project.book_store.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.ResetPasswordToken;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.exception.ExpiredTokenException;
import com.second_project.book_store.exception.ResetPasswordTokenNotFoundException;
import com.second_project.book_store.repository.ResetPasswordTokenRepository;
import com.second_project.book_store.service.ResetPasswordTokenService;

@Service
public class ResetPasswordTokenServiceImpl implements ResetPasswordTokenService {

    private final ResetPasswordTokenRepository resetPasswordTokenRepository;

    public ResetPasswordTokenServiceImpl(ResetPasswordTokenRepository resetPasswordTokenRepository) {
        this.resetPasswordTokenRepository = resetPasswordTokenRepository;
    }

    @Override
    @Transactional
    public ResetPasswordToken createResetPasswordToken(User user) {
        // Delete existing token if any (user can only have one active reset token)
        Optional<ResetPasswordToken> existingToken = resetPasswordTokenRepository.findByUserUserId(user.getUserId());
        existingToken.ifPresent(resetPasswordTokenRepository::delete);

        // Create new token
        ResetPasswordToken resetPasswordToken = new ResetPasswordToken(user);
        return resetPasswordTokenRepository.save(resetPasswordToken);
    }

    @Override
    public ResetPasswordToken findByToken(String token) {
        return resetPasswordTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResetPasswordTokenNotFoundException("Reset password token not found: " + token));
    }

    @Override
    public ResetPasswordToken findByUser(User user) {
        return resetPasswordTokenRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResetPasswordTokenNotFoundException("Reset password token not found for user: " + user.getUserId()));
    }

    @Override
    @Transactional
    public void verifyToken(String token) {
        // Load token within the same transaction to ensure it's managed
        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResetPasswordTokenNotFoundException("Reset password token not found: " + token));

        if (!resetPasswordToken.isValidToken()) {
            // Delete invalid/expired token before throwing exception
            Long tokenId = resetPasswordToken.getResetPasswordTokenId();
            resetPasswordTokenRepository.deleteById(tokenId);
            resetPasswordTokenRepository.flush(); // Force immediate deletion
            throw new ExpiredTokenException();
        }

        // Token is valid - user can proceed with password reset
        // Token will be deleted after successful password reset
    }

    @Override
    @Transactional
    public void deleteToken(ResetPasswordToken token) {
        resetPasswordTokenRepository.delete(token);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        resetPasswordTokenRepository.deleteExpiredTokens();
    }
}

