package com.example.backend.service;

public interface EmailService {
    void sendCredentials(String toEmail, String username, String rawPassword);
}
