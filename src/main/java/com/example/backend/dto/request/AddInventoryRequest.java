package com.example.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddInventoryRequest {

    @NotNull
    private Integer medicineId;

    @NotNull
    @Min(0)
    private Integer stock;

    @NotNull
    private BigDecimal price;
}
