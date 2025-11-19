package com.second_project.book_store.service;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.VerificationToken;

public interface VerificationTokenService {

    VerificationToken createVerificationToken(User user);

    VerificationToken findByToken(String token);

    VerificationToken findByUser(User user);

    User verifyTokenAndReturnUser(String token);

    void deleteToken(VerificationToken token);

    void deleteExpiredToken();

    void deleteInvalidToken();

    void deleteByToken(String token);

    User findUserByToken(String token);

    void deleteByUserId(Long userId);
}

