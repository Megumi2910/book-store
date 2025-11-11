package com.second_project.book_store.service;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String subject, String body);

}
