package com.second_project.book_store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.VerificationToken;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiredAt < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}

