package com.example.backend.dto.request;

import com.example.backend.entity.CatalogStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminMedicineCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String regNo;

    private String genericName;
    private String brandName;
    private String manufacturer;
    private String country;

    private CatalogStatus status = CatalogStatus.ACTIVE;
}
