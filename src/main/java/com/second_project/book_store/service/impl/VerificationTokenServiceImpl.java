package com.second_project.book_store.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.VerificationToken;
import com.second_project.book_store.exception.ExpiredTokenException;
import com.second_project.book_store.exception.UserNotFoundException;
import com.second_project.book_store.exception.VerificationTokenNotFoundException;
import com.second_project.book_store.repository.UserRepository;
import com.second_project.book_store.repository.VerificationTokenRepository;
import com.second_project.book_store.service.VerificationTokenService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public VerificationTokenServiceImpl(VerificationTokenRepository verificationTokenRepository,
                                       UserRepository userRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public VerificationToken createVerificationToken(User user) {
        // (Step 1: Get the ID before we lose it)
        // Reload User entity fresh from database to clear any stale references
        // This prevents ObjectOptimisticLockingFailureException when token was deleted in separate transaction
        Long userId = user.getUserId();

        // (Step 2: Fetch FRESH user from database
        // This user has NO stale references - it's current!)
        User freshUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        // freshUser.verificationToken = null ✅ (token was deleted)

        // (Step 3: Detach the OLD user
        // Tell Hibernate: "Stop tracking this old user object"
        // This prevents Hibernate from trying to sync stale references)
        // Detach the old user entity to prevent stale reference issues
        entityManager.detach(user);
        // Now Hibernate ignores the old user completely
        
        // (Step 4: Delete any existing token (if any)
        // This is safe because freshUser has no stale references)
        // Delete existing token if any - use repository method for reliable deletion
        // This ensures the unique constraint on user_id is satisfied
        // The @Modifying query bypasses entity manager cache, so it's safe
        verificationTokenRepository.deleteByUserId(userId);
        verificationTokenRepository.flush(); // Force immediate deletion before creating new token

        // (Step 5: Clear the relationship on fresh user)
        // Clear bidirectional relationship reference on fresh User entity
        freshUser.setVerificationToken(null);

        // (Step 6: Create new token with FRESH user)
        // Create new token with fresh User entity
        VerificationToken verificationToken = new VerificationToken(freshUser);
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
    public User verifyTokenAndReturnUser(String token) {
        // Load token within the same transaction to ensure it's managed
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new VerificationTokenNotFoundException("Verification token not found: " + token));

        if (!verificationToken.isValidToken()){
            Long tokenId = verificationToken.getVerificationTokenId();
            verificationTokenRepository.deleteById(tokenId);
            verificationTokenRepository.flush();
            throw new ExpiredTokenException();
        }

        // Enable user and clear token reference
        User user = verificationToken.getUser();
        user.setEnabled(true);
        //user.setVerificationToken(null); // Clear bidirectional relationship reference

        return userRepository.save(user);
    }


    @Override
    @Transactional
    public void deleteToken(VerificationToken token) {
        verificationTokenRepository.delete(token);
    }

    @Override
    @Transactional
    public void deleteExpiredToken() {
        verificationTokenRepository.deleteExpiredToken();
    }

    @Override
    @Transactional
    public void deleteByToken(String token){
        verificationTokenRepository.deleteByToken(token);
        verificationTokenRepository.flush();
    }
   
    @Override
    @Transactional
    public void deleteByUserId(Long userId){
        verificationTokenRepository.deleteByUserId(userId);
    }


    @Override
    @Transactional
    public void deleteInvalidToken() {
        verificationTokenRepository.deleteInvalidToken();
    }

    @Override
    public User findUserByToken(String token) {
        return verificationTokenRepository.findUserByToken(token);
    }

}

