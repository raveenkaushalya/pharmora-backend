package com.example.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.backend.dto.request.AdminMedicineCreateRequest;
import com.example.backend.dto.request.AdminMedicineUpdateRequest;
import com.example.backend.dto.response.AdminMedicineRowResponse;
import com.example.backend.dto.response.AdminMedicineSuggestItem;

public interface AdminMedicineService {
        long count();
    Page<AdminMedicineRowResponse> list(String q, String status, String manufacturer, String country, String brandName, int page, int size, String sort);
    AdminMedicineRowResponse create(AdminMedicineCreateRequest req);
    AdminMedicineRowResponse update(Integer id, AdminMedicineUpdateRequest req);
    void delete(Integer id);


    AdminMedicineRowResponse getOne(Integer id);
    List<AdminMedicineSuggestItem> suggest(String q);
    List<String> manufacturers();
    List<String> brands();

}
