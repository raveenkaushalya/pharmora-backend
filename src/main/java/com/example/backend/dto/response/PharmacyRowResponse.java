package com.example.backend.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyRowResponse {

    private Integer id;

    private String legalEntityName;
    private String tradeName;
    private String nmraLicense;
    private String businessRegNo;

    private String addressInSriLanka;
    private String telephone;
    private String email;

    private String entityType;

    private String contactFullName;
    private String contactTitle;
    private String contactPhone;
    private String contactEmail;

    private LocalDate declarationDate;
    private boolean agreedToDeclaration;

    private String status;  // PENDING / APPROVED / REJECTED
    private String rejectionReason;

    private LocalDateTime createdAt; // ✅ submitted date for table
}
