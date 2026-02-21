package com.example.backend.service;

import org.springframework.data.domain.Page;

import com.example.backend.dto.request.PharmacyRegisterRequest;
import com.example.backend.dto.response.PharmacyApproveResponse;
import com.example.backend.dto.response.PharmacyRowResponse;

public interface PharmacyService {
    long countAll();
    long countByStatus(String status);

    Integer register(PharmacyRegisterRequest request);

    Page<PharmacyRowResponse> getPharmaciesForAdmin(String status, String q, int page, int size);

    PharmacyApproveResponse approvePharmacy(Integer pharmacyId);

    void rejectPharmacy(Integer pharmacyId, String reason);
    Object getPharmaciesByStatus(String status, int page, int size);

}
