package com.example.backend.service;

import java.util.List;

import com.example.backend.dto.response.MedicineMasterDto;
import com.example.backend.dto.response.MedicineSuggestResponse;
import com.example.backend.entity.MedicineMaster;

public interface MedicineCatalogService {
    List<MedicineSuggestResponse> suggest(String q);

    MedicineMaster getOne(Integer id);

    List<MedicineMasterDto> getAll();
}
