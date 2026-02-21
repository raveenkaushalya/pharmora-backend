package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InventoryActivityResponse {

    private Integer id;
    private String action;
    private String message;
    private Integer medicineId;
    private String medicineName;
    private LocalDateTime createdAt;
}
