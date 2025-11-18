package com.second_project.book_store.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.ResetPasswordToken;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.User.UserRole;
import com.second_project.book_store.event.PasswordResetRequestEvent;
import com.second_project.book_store.event.RegistrationCompleteEvent;
import com.second_project.book_store.exception.InvalidPasswordException;
import com.second_project.book_store.exception.ResetPasswordTokenNotFoundException;
import com.second_project.book_store.exception.UserAlreadyEnabledException;
import com.second_project.book_store.exception.UserNotFoundException;
import com.second_project.book_store.model.ChangePasswordRequestDto;
import com.second_project.book_store.model.ResetPasswordRequestDto;
import com.second_project.book_store.model.UserDto;
import com.second_project.book_store.repository.ResetPasswordTokenRepository;
import com.second_project.book_store.repository.UserRepository;
import com.second_project.book_store.service.ResetPasswordTokenService;
import com.second_project.book_store.service.UserService;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final ResetPasswordTokenService resetPasswordTokenService;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          ApplicationEventPublisher eventPublisher,
                          ResetPasswordTokenService resetPasswordTokenService,
                          ResetPasswordTokenRepository resetPasswordTokenRepository){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.resetPasswordTokenService = resetPasswordTokenService;
        this.resetPasswordTokenRepository = resetPasswordTokenRepository;
    }

    @Override
    @Transactional
    public User registerUser(UserDto userDto) {
        User user = new User();

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(UserRole.USER);
        // user.setEnabled(false); default is false already

        user = userRepository.save(user);
        
        // Publish event - listeners will handle token creation, email sending, etc.
        // Email URL is configured via FrontendProperties (application.yml)
        eventPublisher.publishEvent(new RegistrationCompleteEvent(user));

        return user;
    }

    @Override
    @Transactional
    public void resendVerificationToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        
        // Check if user is already enabled/verified
        if (user.isEnabled()) {
            throw new UserAlreadyEnabledException("User is already verified. No need to resend verification token.");
        }
        
        // Publish event - existing listener will handle token creation and email sending
        // Email URL is configured via FrontendProperties (application.yml)
        eventPublisher.publishEvent(new RegistrationCompleteEvent(user));
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        
        // Check if user is enabled (only enabled users can reset password)
        if (!user.isEnabled()) {
            throw new UserNotFoundException("User account is not verified. Please verify your account first.");
        }
        
        // Publish event - listener will handle token creation and email sending
        // Email URL is configured via FrontendProperties (application.yml)
        eventPublisher.publishEvent(new PasswordResetRequestEvent(user));
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto resetPasswordRequestDto) {
        // Verify token is valid
        resetPasswordTokenService.verifyToken(resetPasswordRequestDto.getToken());
        
        // Find token and user
        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findByToken(resetPasswordRequestDto.getToken())
                .orElseThrow(() -> new ResetPasswordTokenNotFoundException("Reset password token not found"));
        
        User user = resetPasswordToken.getUser();
        
        // Update password
        user.setPassword(passwordEncoder.encode(resetPasswordRequestDto.getPassword()));
        userRepository.save(user);
        
        // Delete the used token
        resetPasswordTokenRepository.deleteById(resetPasswordToken.getResetPasswordTokenId());
        resetPasswordTokenRepository.flush(); // Force immediate deletion
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequestDto changePasswordRequestDto) {
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        // Verify current password matches
        if (!passwordEncoder.matches(changePasswordRequestDto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        
        // Verify new password is different from current password
        if (passwordEncoder.matches(changePasswordRequestDto.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("New password must be different from current password");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordRequestDto.getPassword()));
        userRepository.save(user);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
}
