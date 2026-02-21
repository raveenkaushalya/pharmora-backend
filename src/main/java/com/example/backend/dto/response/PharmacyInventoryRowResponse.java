package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PharmacyInventoryRowResponse {

    private Integer id;

    // medicine details
    private Integer medicineId;
    private String regNo;
    private String genericName;
    private String brandName;
    private String dosage;
    private String manufacturer;
    private String country;

    // pharmacy fields
    private Integer stock;
    private BigDecimal price;
}
