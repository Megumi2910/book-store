package com.second_project.book_store.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.VerificationToken;
import com.second_project.book_store.exception.ExpiredTokenException;
import com.second_project.book_store.exception.VerificationTokenNotFoundException;
import com.second_project.book_store.repository.UserRepository;
import com.second_project.book_store.repository.VerificationTokenRepository;
import com.second_project.book_store.service.VerificationTokenService;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;

    public VerificationTokenServiceImpl(VerificationTokenRepository verificationTokenRepository,
                                       UserRepository userRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public VerificationToken createVerificationToken(User user) {
        // Delete existing token if any
        Optional<VerificationToken> existingToken = verificationTokenRepository.findByUserUserId(user.getUserId());
        existingToken.ifPresent(verificationTokenRepository::delete);

        // Create new token
        VerificationToken verificationToken = new VerificationToken(user);
        return verificationTokenRepository.save(verificationToken);
    }

    @Override
    public VerificationToken findByToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new VerificationTokenNotFoundException("Verification token not found: " + token));
    }

    @Override
    public VerificationToken findByUser(User user) {
        return verificationTokenRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new VerificationTokenNotFoundException("Verification token not found for user: " + user.getUserId()));
    }

    @Override
    @Transactional
    public void verifyToken(String token) {
        // Load token within the same transaction to ensure it's managed
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new VerificationTokenNotFoundException("Verification token not found: " + token));

        if (!verificationToken.isValidToken()){
            // Delete invalid/expired token before throwing exception
            Long tokenId = verificationToken.getVerificationTokenId();
            verificationTokenRepository.deleteById(tokenId);
            verificationTokenRepository.flush(); // Force immediate deletion
            throw new ExpiredTokenException();
        }

        // Enable user and clear token reference
        User user = verificationToken.getUser();
        user.setEnabled(true);
        user.setVerificationToken(null); // Clear bidirectional relationship reference
        userRepository.save(user);

        // Delete token after successful verification
        // Use deleteById for reliable deletion (works even if entity is detached)
        Long tokenId = verificationToken.getVerificationTokenId();
        verificationTokenRepository.deleteById(tokenId);
        verificationTokenRepository.flush(); // Force immediate deletion to ensure it's persisted
    }

    @Override
    @Transactional
    public void deleteToken(VerificationToken token) {
        verificationTokenRepository.delete(token);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        verificationTokenRepository.deleteExpiredTokens();
    }

}

