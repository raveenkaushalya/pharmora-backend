package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MedicineSuggestResponse {
    private Integer id;
    private String regNo;
    private String genericName;
    private String brandName;
    private String dosage;
}
