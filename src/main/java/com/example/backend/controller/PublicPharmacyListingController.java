package com.example.backend.controller;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PublicPharmacyWithInventoryResponse;
import com.example.backend.entity.Pharmacy;
import com.example.backend.entity.PharmacyInventoryItem;
import com.example.backend.repository.PharmacyInventoryRepository;
import com.example.backend.repository.PharmacyLocationRepository;
import com.example.backend.repository.PharmacyRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/pharmacies-with-inventory")
@CrossOrigin(origins = {
    "http://localhost:3000",
    "http://localhost:5173",
    "https://raveenkaushalya.github.io",
    "https://raveenkaushalya.github.io/Medicine-Availability-Tracker/"
}, allowCredentials = "true")
@RequiredArgsConstructor
public class PublicPharmacyListingController {
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyInventoryRepository inventoryRepository;
    private final PharmacyLocationRepository pharmacyLocationRepository;

    @GetMapping
    public ApiResponse getPharmaciesWithAvailableInventory() {
        // Get all pharmacies
        List<Pharmacy> pharmacies = pharmacyRepository.findAll();
        // For each pharmacy, get all inventory (including out of stock)
        List<PublicPharmacyWithInventoryResponse> result = pharmacies.stream()
            .map(pharmacy -> {
                List<PharmacyInventoryItem> inventory = inventoryRepository.findByPharmacyOrderByIdDesc(pharmacy);
                if (inventory.isEmpty()) return null;
                // Fetch location
                var locOpt = pharmacyLocationRepository.findByPharmacy(pharmacy);
                Double lat = locOpt.map(l -> l.getLatitude()).orElse(null);
                Double lng = locOpt.map(l -> l.getLongitude()).orElse(null);
                return PublicPharmacyWithInventoryResponse.from(pharmacy, inventory, lat, lng);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return new ApiResponse(true, "OK", result);
    }
}