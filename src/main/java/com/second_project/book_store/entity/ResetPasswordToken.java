package com.second_project.book_store.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Entity for password reset tokens.
 * Separate from VerificationToken to maintain clear separation of concerns.
 * 
 * Token expiration: 15 minutes (longer than verification token since user might need more time)
 */
@Entity
@Table(indexes = {
    @Index(name = "idx_reset_password_token", columnList = "token")
})
public class ResetPasswordToken {

    // Default duration - can be overridden via TokenProperties
    private static final int TOKEN_DURATION_MINUTES = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resetPasswordTokenId;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_USER_RESET_PASSWORD_TOKEN")
    )
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private boolean isValid = true;

    public ResetPasswordToken() {
    }

    /**
     * Creates a new reset password token for the given user.
     * Token expires in 15 minutes (default).
     */
    public ResetPasswordToken(User user) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiredAt = LocalDateTime.now().plusMinutes(TOKEN_DURATION_MINUTES);
        this.isValid = true;
    }

    /**
     * Creates a new reset password token for the given user with custom duration.
     * 
     * @param user The user for whom the token is created
     * @param durationMinutes Token expiration duration in minutes
     */
    public ResetPasswordToken(User user, int durationMinutes) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiredAt = LocalDateTime.now().plusMinutes(durationMinutes);
        this.isValid = true;
    }

    /**
     * Checks if the token has expired.
     * 
     * @return true if current time is after expiration time
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    /**
     * Checks if the token is valid (not expired and not invalidated).
     * 
     * @return true if token is valid and not expired
     */
    public boolean isValidToken() {
        return isValid && !isExpired();
    }

    // Getters and Setters
    public Long getResetPasswordTokenId() {
        return resetPasswordTokenId;
    }

    public void setResetPasswordTokenId(Long resetPasswordTokenId) {
        this.resetPasswordTokenId = resetPasswordTokenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public String toString() {
        return "ResetPasswordToken [resetPasswordTokenId=" + resetPasswordTokenId + ", token=" + token
                + ", expiredAt=" + expiredAt + ", isValid=" + isValid + "]";
    }
}

