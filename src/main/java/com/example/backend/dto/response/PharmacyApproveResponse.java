package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PharmacyApproveResponse {
    private String username;
    private String setupLink;
    private LocalDateTime expiresAt;
}
