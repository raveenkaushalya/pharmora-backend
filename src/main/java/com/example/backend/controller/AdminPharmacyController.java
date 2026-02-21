package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.request.PharmacyRejectRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PharmacyApproveResponse;
import com.example.backend.service.PharmacyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/pharmacies")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AdminPharmacyController {
    @GetMapping("/count")
    public long count(@RequestParam(defaultValue = "ALL") String status) {
        if (status == null || status.equalsIgnoreCase("ALL")) {
            return pharmacyService.countAll();
        } else {
            return pharmacyService.countByStatus(status);
        }
    }

    private final PharmacyService pharmacyService;

    public AdminPharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    // Admin table list (pagination + filter + search)
    // Examples:
    // /api/v1/admin/pharmacies?status=PENDING&page=0&size=10
    // /api/v1/admin/pharmacies?status=APPROVED&q=abc&page=0&size=10

    @GetMapping
    public ResponseEntity<ApiResponse> list(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                new ApiResponse(true, "OK", pharmacyService.getPharmaciesByStatus(status, page, size))
        );
    }

    // Approve pharmacy
    @PatchMapping("/{id}/approve")
    public ApiResponse approve(@PathVariable Integer id) {

        PharmacyApproveResponse response = pharmacyService.approvePharmacy(id);

        return new ApiResponse(
                true,
                "Pharmacy approved. Share this setup link with the pharmacy to set their password.",
                response
        );
    }


    // Reject pharmacy
    @PatchMapping("/{id}/reject")
    public ApiResponse reject(@PathVariable Integer id, @Valid @RequestBody PharmacyRejectRequest request) {
        pharmacyService.rejectPharmacy(id, request.getReason());
        return new ApiResponse(true, "Pharmacy rejected.", null);
    }
}
