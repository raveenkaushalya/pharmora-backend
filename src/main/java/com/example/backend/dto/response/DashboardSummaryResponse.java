package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalMedicines;
    private long activePharmacies;
    private long pendingReviews;

    private List<StockSlice> stockStatus;

    @Data
    @AllArgsConstructor
    public static class StockSlice {
        private String name; // "In Stock", "Low Stock", "Out of Stock"
        private long value;
    }
}
