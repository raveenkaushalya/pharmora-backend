package com.example.backend.controller;

import com.example.backend.dto.request.PharmacyRegisterRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.service.PharmacyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pharmacies")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})  // React dev server (can change later)
public class PharmacyController {

    private final PharmacyService pharmacyService;

    public PharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    // Pharmacy register (Public)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody PharmacyRegisterRequest request) {

        Integer id = pharmacyService.register(request);

        return ResponseEntity.ok(
                new ApiResponse(true, "Pharmacy details submitted successfully (PENDING)", id)
        );
    }
}
