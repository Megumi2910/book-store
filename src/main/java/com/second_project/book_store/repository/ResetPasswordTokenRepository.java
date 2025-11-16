package com.second_project.book_store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.ResetPasswordToken;

@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, Long> {

    Optional<ResetPasswordToken> findByToken(String token);

    Optional<ResetPasswordToken> findByUserUserId(Long userId);

    /**
     * Deletes all expired reset password tokens.
     * Can be called periodically via a scheduled task.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ResetPasswordToken rpt WHERE rpt.expiredAt < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();

    /**
     * Deletes reset password token for a specific user.
     * Useful when user successfully resets password or requests a new token.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ResetPasswordToken rpt WHERE rpt.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}

