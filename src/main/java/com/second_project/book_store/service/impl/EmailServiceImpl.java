package com.second_project.book_store.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.second_project.book_store.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService{

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender javaMailSender;

    public EmailServiceImpl(JavaMailSender javaMailSender){
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String subject, String body) {
        
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        javaMailSender.send(message);

        System.out.println("Verification Email Sent..");
    }

    
}
