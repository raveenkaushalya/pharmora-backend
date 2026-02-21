package com.example.backend.controller;

import com.example.backend.dto.response.MedicineMasterDto;
import com.example.backend.dto.response.MedicineSuggestResponse;
import com.example.backend.entity.MedicineMaster;
import com.example.backend.service.MedicineCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineCatalogController {

    // Public endpoint: get all medicines (read-only)
    @GetMapping("/all")
    public List<MedicineMasterDto> getAll() {
        return medicineCatalogService.getAll();
    }

    private final MedicineCatalogService medicineCatalogService;

    // Example: /api/medicines/suggest?q=para
    @GetMapping("/suggest")
    public List<MedicineSuggestResponse> suggest(@RequestParam String q) {
        return medicineCatalogService.suggest(q);
    }
    @GetMapping("/{id}")
    public MedicineMaster getOne(@PathVariable Integer id) {
        return medicineCatalogService.getOne(id);
    }

}
