
    package com.example.backend.service;

import com.example.backend.dto.request.SetPasswordRequest;


public interface PasswordSetupService {
    String generateResetToken(String email);
    void setPassword(SetPasswordRequest req);
}
