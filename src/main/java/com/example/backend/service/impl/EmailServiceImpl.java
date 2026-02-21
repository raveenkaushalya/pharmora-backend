package com.example.backend.service.impl;

import com.example.backend.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendCredentials(String toEmail, String username, String rawPassword) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Pharmacy Account Approved - Login Credentials");
        msg.setText(
                "Your pharmacy account has been approved.\n\n" +
                        "Username: " + username + "\n" +
                        "Password: " + rawPassword + "\n\n" +
                        "Please change your password after logging in."
        );
        mailSender.send(msg);
    }
}
