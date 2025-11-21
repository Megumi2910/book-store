package com.second_project.book_store.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.second_project.book_store.config.properties.TokenProperties;
import com.second_project.book_store.entity.ResetPasswordToken;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.User.UserRole;
import com.second_project.book_store.event.RegistrationCompleteEvent;
import com.second_project.book_store.exception.InvalidPasswordException;
import com.second_project.book_store.exception.RateLimitException;
import com.second_project.book_store.exception.UserAlreadyEnabledException;
import com.second_project.book_store.exception.UserAlreadyExistedException;
import com.second_project.book_store.exception.UserNotFoundException;
import com.second_project.book_store.model.ChangePasswordRequestDto;
import com.second_project.book_store.model.ResetPasswordRequestDto;
import com.second_project.book_store.model.UserDto;
import com.second_project.book_store.repository.ResetPasswordTokenRepository;
import com.second_project.book_store.repository.UserRepository;
import com.second_project.book_store.service.ResetPasswordTokenService;

/**
 * Unit tests for UserServiceImpl.
 * 
 * Testing strategy:
 * - Use Mockito to mock dependencies
 * - Test happy paths and error cases
 * - Verify correct method calls and arguments
 * - Verify exception handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ResetPasswordTokenService resetPasswordTokenService;

    @Mock
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Mock
    private TokenProperties tokenProperties;

    @Mock
    private com.second_project.book_store.config.MetricsConfig metricsConfig;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setEmail("john.doe@example.com");
        userDto.setPassword("Test@123");
        userDto.setMatchingPassword("Test@123");

        user = new User();
        user.setUserId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.USER);
        user.setEnabled(false);
    }

    @Test
    @DisplayName("Should register user successfully with valid data")
    void registerUser_WithValidData_ShouldSucceed() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.registerUser(userDto);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals(UserRole.USER, result.getRole());
        
        verify(userRepository).findByEmailIgnoreCase("john.doe@example.com");
        verify(passwordEncoder).encode("Test@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistedException when email already exists")
    void registerUser_WithDuplicateEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));

        // Act & Assert
        UserAlreadyExistedException exception = assertThrows(
            UserAlreadyExistedException.class,
            () -> userService.registerUser(userDto)
        );

        assertTrue(exception.getMessage().contains("john.doe@example.com"));
        verify(userRepository).findByEmailIgnoreCase("john.doe@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should request verification email successfully")
    void requestVerificationEmail_WithValidEmail_ShouldSucceed() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.requestVerificationEmail("john.doe@example.com");

        // Assert
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEvent(any(RegistrationCompleteEvent.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when requesting verification for non-existent user")
    void requestVerificationEmail_WithNonExistentEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UserNotFoundException.class,
            () -> userService.requestVerificationEmail("nonexistent@example.com")
        );

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should throw UserAlreadyEnabledException when user is already verified")
    void requestVerificationEmail_WithAlreadyEnabledUser_ShouldThrowException() {
        // Arrange
        user.setEnabled(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(
            UserAlreadyEnabledException.class,
            () -> userService.requestVerificationEmail("john.doe@example.com")
        );

        verify(userRepository).findByEmail("john.doe@example.com");
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should throw RateLimitException when verification email requested too soon")
    void requestVerificationEmail_WithinRateLimit_ShouldThrowException() {
        // Arrange
        user.setLastVerificationEmailSent(LocalDateTime.now().minusSeconds(30));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(tokenProperties.getRateLimitSeconds()).thenReturn(60L);

        // Act & Assert
        RateLimitException exception = assertThrows(
            RateLimitException.class,
            () -> userService.requestVerificationEmail("john.doe@example.com")
        );

        assertTrue(exception.getSecondsRemaining() > 0);
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should change password successfully with valid current password")
    void changePassword_WithValidCurrentPassword_ShouldSucceed() {
        // Arrange
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto();
        dto.setCurrentPassword("oldPassword");
        dto.setPassword("NewTest@123");
        dto.setMatchingPassword("NewTest@123");

        user.setPassword("encodedOldPassword");
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewTest@123", "encodedOldPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewTest@123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.changePassword(1L, dto);

        // Assert
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
        verify(passwordEncoder).encode("NewTest@123");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException when current password is incorrect")
    void changePassword_WithIncorrectCurrentPassword_ShouldThrowException() {
        // Arrange
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto();
        dto.setCurrentPassword("wrongPassword");
        dto.setPassword("NewTest@123");
        dto.setMatchingPassword("NewTest@123");

        user.setPassword("encodedOldPassword");
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(
            InvalidPasswordException.class,
            () -> userService.changePassword(1L, dto)
        );

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", "encodedOldPassword");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException when new password same as current")
    void changePassword_WithSamePassword_ShouldThrowException() {
        // Arrange
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto();
        dto.setCurrentPassword("Test@123");
        dto.setPassword("Test@123");
        dto.setMatchingPassword("Test@123");

        user.setPassword("encodedPassword");
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Test@123", "encodedPassword")).thenReturn(true);

        // Act & Assert
        assertThrows(
            InvalidPasswordException.class,
            () -> userService.changePassword(1L, dto)
        );

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reset password successfully with valid token")
    void resetPassword_WithValidToken_ShouldSucceed() {
        // Arrange
        ResetPasswordRequestDto dto = new ResetPasswordRequestDto();
        dto.setToken("valid-token");
        dto.setPassword("NewTest@123");
        dto.setMatchingPassword("NewTest@123");

        ResetPasswordToken token = new ResetPasswordToken();
        token.setResetPasswordTokenId(1L);
        token.setToken("valid-token");
        token.setUser(user);

        doNothing().when(resetPasswordTokenService).verifyToken(anyString());
        when(resetPasswordTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.resetPassword(dto);

        // Assert
        verify(resetPasswordTokenService).verifyToken("valid-token");
        verify(resetPasswordTokenRepository).findByToken("valid-token");
        verify(passwordEncoder).encode("NewTest@123");
        verify(userRepository).save(user);
        verify(resetPasswordTokenRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should find user by email successfully")
    void findUserByEmail_WithExistingEmail_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Act
        User result = userService.findUserByEmail("john.doe@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when email does not exist")
    void findUserByEmail_WithNonExistentEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UserNotFoundException.class,
            () -> userService.findUserByEmail("nonexistent@example.com")
        );

        verify(userRepository).findByEmail("nonexistent@example.com");
    }
}

