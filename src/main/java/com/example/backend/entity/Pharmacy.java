package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Data
@NoArgsConstructor
@AllArgsConstructor


@Entity
@Table(
        name = "pharmacy",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "business_reg_no"),
                @UniqueConstraint(columnNames = "nmra_license")
        }
)
public class Pharmacy {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ===== Business Details =====
    private String legalEntityName;
    private String tradeName;
    private String nmraLicense;
    private String businessRegNo;
    private String addressInSriLanka;
    private String telephone;
    private String email;
    private String entityType;

    // ===== Contact Person =====
    private String contactFullName;
    private String contactTitle;
    private String contactPhone;
    private String contactEmail;

    // ===== Declaration =====
    private LocalDate declarationDate;
    private boolean agreedToDeclaration;


    @Column(columnDefinition = "TEXT")
    private String aboutPharmacy;         // About your pharmacy

    @Column(columnDefinition = "TEXT")
    private String openingHoursJson;      // store opening hours as JSON


    // ===== Workflow =====
    @Enumerated(EnumType.STRING)
    private PharmacyStatus status;

    private String rejectionReason;

    private LocalDateTime createdAt;
}
