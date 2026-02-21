package com.example.backend.dto.response;

import com.example.backend.entity.CatalogStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminMedicineRowResponse {
    private Integer id;
    private String genericName;
    private String brandName;
    private String manufacturer;
    private String country;
    private String regNo;
    private CatalogStatus status;
}
