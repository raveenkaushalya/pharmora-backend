package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MedicineMasterDto {
    private Integer id;
    private String genericName;
    private String brandName;
    private String manufacturer;
    private String country;
    private String regNo;
    private String status;
    private String dosage;
}
