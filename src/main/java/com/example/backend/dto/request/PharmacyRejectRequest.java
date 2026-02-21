package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyRejectRequest {

    @NotBlank
    private String reason;
}
