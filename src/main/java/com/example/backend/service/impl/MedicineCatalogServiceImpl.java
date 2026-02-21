package com.example.backend.service.impl;

import com.example.backend.dto.response.MedicineSuggestResponse;
import com.example.backend.entity.MedicineMaster;
import com.example.backend.repository.MedicineMasterRepository;
import com.example.backend.service.MedicineCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineCatalogServiceImpl implements MedicineCatalogService {
    @Override
    public java.util.List<com.example.backend.dto.response.MedicineMasterDto> getAll() {
        return medicineRepo.findAll().stream().map(m -> new com.example.backend.dto.response.MedicineMasterDto(
            m.getId(),
            m.getGenericName(),
            m.getBrandName(),
            m.getManufacturer(),
            m.getCountry(),
            m.getRegNo(),
            m.getStatus() != null ? m.getStatus().name() : null,
            m.getDosage()
        )).toList();
    }

    private final MedicineMasterRepository medicineRepo;

    @Override
    public MedicineMaster getOne(Integer id) {
        return medicineRepo.findById(id).orElseThrow(() -> new RuntimeException("Medicine not found"));
    }

    @Override
    public List<MedicineSuggestResponse> suggest(String q) {
        if (q == null) return Collections.emptyList();
        q = q.trim();
        if (q.length() < 2) return Collections.emptyList(); // prevent heavy queries

        List<MedicineMaster> list =
                medicineRepo.findTop10ByGenericNameStartingWithIgnoreCaseOrBrandNameStartingWithIgnoreCase(q, q);

        return list.stream()
                .map(m -> new MedicineSuggestResponse(
                        m.getId(),
                        m.getRegNo(),
                        m.getGenericName(),
                        m.getBrandName(),
                        m.getDosage()
                ))
                .toList();
    }
}
