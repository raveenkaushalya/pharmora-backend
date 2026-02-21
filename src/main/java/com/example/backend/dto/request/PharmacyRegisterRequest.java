package com.example.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyRegisterRequest {

    // ===== Business step =====
    @NotBlank
    private String legalEntityName;

    private String tradeName;

    @NotBlank
    private String nmraLicense;

    @NotBlank
    private String businessRegNo;

    @NotBlank
    private String addressInSriLanka;

    @NotBlank
    private String telephone;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String entityType;

    // ===== Contact step =====
    @NotBlank
    private String contactFullName;

    @NotBlank
    private String contactTitle;

    @NotBlank
    private String contactPhone;

    @Email
    @NotBlank
    private String contactEmail;

    // ===== Declaration step =====
    @NotNull
    private LocalDate declarationDate;

    private boolean agreedToDeclaration;
}
