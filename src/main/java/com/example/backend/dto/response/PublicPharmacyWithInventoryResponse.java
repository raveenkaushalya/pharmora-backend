package com.example.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.example.backend.entity.MedicineMaster;
import com.example.backend.entity.Pharmacy;
import com.example.backend.entity.PharmacyInventoryItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicPharmacyWithInventoryResponse {
    private Integer id;
    private String name;
    private String address;
    private String phone;
    private String hours;
    private String type;
    private Boolean isOpen;
    @com.fasterxml.jackson.annotation.JsonProperty("latitude")
    private Double latitude;
    @com.fasterxml.jackson.annotation.JsonProperty("longitude")
    private Double longitude;
    private List<InventoryItem> inventory;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @com.fasterxml.jackson.annotation.JsonAutoDetect(fieldVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY)
    public static class InventoryItem {
        private Integer medicineId;
        private String drugName;
        @com.fasterxml.jackson.annotation.JsonProperty("brandName")
        private String brandName;
        private String dosage;
        private Integer quantity;
        private BigDecimal price;
        private Boolean inStock;
        @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        private java.time.LocalDateTime updatedAt;
        private String category;
    }

    public static PublicPharmacyWithInventoryResponse from(Pharmacy p, List<PharmacyInventoryItem> inventory, Double latitude, Double longitude) {
        return PublicPharmacyWithInventoryResponse.builder()
            .id(p.getId())
            .name(p.getTradeName() != null && !p.getTradeName().isEmpty() ? p.getTradeName() : p.getLegalEntityName())
            .address(p.getAddressInSriLanka())
            .phone(p.getTelephone())
            .hours(p.getOpeningHoursJson())
            .type(p.getEntityType())
            .isOpen(true)
            .latitude(latitude)
            .longitude(longitude)
            .inventory(inventory.stream().map(item -> {
                MedicineMaster med = item.getMedicine();
                java.time.LocalDateTime updated = item.getUpdatedAt();
                if (updated == null) updated = java.time.LocalDateTime.now();
                String category = "";
                return InventoryItem.builder()
                    .medicineId(med.getId())
                    .drugName(med.getGenericName() != null ? med.getGenericName() : med.getBrandName())
                    .brandName(med.getBrandName())
                    .dosage(med.getDosage())
                    .quantity(item.getStock())
                    .price(item.getPrice())
                    .inStock(item.getStock() > 0)
                    .updatedAt(updated)
                    .category(category)
                    .build();
            }).collect(Collectors.toList()))
            .build();
    }
}