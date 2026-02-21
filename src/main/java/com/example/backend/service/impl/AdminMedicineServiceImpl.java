package com.example.backend.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.request.AdminMedicineCreateRequest;
import com.example.backend.dto.request.AdminMedicineUpdateRequest;
import com.example.backend.dto.response.AdminMedicineRowResponse;
import com.example.backend.dto.response.AdminMedicineSuggestItem;
import com.example.backend.entity.CatalogStatus;
import com.example.backend.entity.MedicineMaster;
import com.example.backend.repository.MedicineMasterRepository;
import com.example.backend.service.AdminMedicineService;

import jakarta.persistence.criteria.Predicate;

@Service
@Transactional
public class AdminMedicineServiceImpl implements AdminMedicineService {
    @Override
    public long count() {
        return repo.count();
    }

    private final MedicineMasterRepository repo;

    public AdminMedicineServiceImpl(MedicineMasterRepository repo) {
        this.repo = repo;
    }
    private AdminMedicineRowResponse mapRow(MedicineMaster m) {
        return new AdminMedicineRowResponse(
                m.getId(),
                m.getGenericName(),
                m.getBrandName(),
                m.getManufacturer(),
                m.getCountry(),
                m.getRegNo(),
                m.getStatus()
        );
    }

    @Override
    public List<String> manufacturers() {
        return repo.findDistinctManufacturers();
    }

    @Override
    public List<String> brands() {
        return repo.findDistinctBrandNames();
    }


    @Override
    public Page<AdminMedicineRowResponse> list(String q, String status, String manufacturer, String country, String brandName, int page, int size, String sort)
    {

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));

        Specification<MedicineMaster> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();

            if (q != null && !q.trim().isEmpty()) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                Predicate p1 = cb.like(cb.lower(root.get("genericName")), like);
                Predicate p2 = cb.like(cb.lower(root.get("brandName")), like);
                Predicate p3 = cb.like(cb.lower(root.get("regNo")), like);
                p = cb.and(p, cb.or(p1, p2, p3));
            }

            if (status != null && !status.equalsIgnoreCase("ALL")) {
                CatalogStatus st = CatalogStatus.valueOf(status.toUpperCase(Locale.ROOT));
                p = cb.and(p, cb.equal(root.get("status"), st));
            }

            if (manufacturer != null && !manufacturer.trim().isEmpty()) {
                p = cb.and(p, cb.equal(cb.lower(root.get("manufacturer")), manufacturer.trim().toLowerCase(Locale.ROOT)));
            }

            if (brandName != null && !brandName.trim().isEmpty()) {
                p = cb.and(p, cb.equal(cb.lower(root.get("brandName")), brandName.trim().toLowerCase(Locale.ROOT)));
            }

            if (country != null && !country.trim().isEmpty()) {
                p = cb.and(p, cb.equal(cb.lower(root.get("country")), country.trim().toLowerCase(Locale.ROOT)));
            }

            return p;
        };

        Page<MedicineMaster> data = repo.findAll(spec, pageable);

        return data.map(m -> new AdminMedicineRowResponse(
                m.getId(),
                m.getGenericName(),
                m.getBrandName(),
                m.getManufacturer(),
                m.getCountry(),
                m.getRegNo(),
                m.getStatus()
        ));
    }

    @Override
    public AdminMedicineRowResponse create(AdminMedicineCreateRequest req) {

        repo.findByRegNo(req.getRegNo()).ifPresent(x -> {
            throw new IllegalArgumentException("Reg No already exists: " + req.getRegNo());
        });

        MedicineMaster m = new MedicineMaster();
        m.setRegNo(req.getRegNo());
        m.setGenericName(req.getGenericName());
        m.setBrandName(req.getBrandName());
        m.setManufacturer(req.getManufacturer());
        m.setCountry(req.getCountry());
        m.setStatus(req.getStatus());

        MedicineMaster saved = repo.save(m);

        return new AdminMedicineRowResponse(
                saved.getId(),
                saved.getGenericName(),
                saved.getBrandName(),
                saved.getManufacturer(),
                saved.getCountry(),
                saved.getRegNo(),
                saved.getStatus()
        );
    }

    @Override
    public AdminMedicineRowResponse update(Integer id, AdminMedicineUpdateRequest req) {

        MedicineMaster m = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found: " + id));

        // RegNo unique check (only if changed)
        if (!m.getRegNo().equals(req.getRegNo())) {
            repo.findByRegNo(req.getRegNo()).ifPresent(x -> {
                throw new IllegalArgumentException("Reg No already exists: " + req.getRegNo());
            });
        }

        m.setRegNo(req.getRegNo());
        m.setGenericName(req.getGenericName());
        m.setBrandName(req.getBrandName());
        m.setManufacturer(req.getManufacturer());
        m.setCountry(req.getCountry());
        m.setStatus(req.getStatus());

        MedicineMaster saved = repo.save(m);

        return new AdminMedicineRowResponse(
                saved.getId(),
                saved.getGenericName(),
                saved.getBrandName(),
                saved.getManufacturer(),
                saved.getCountry(),
                saved.getRegNo(),
                saved.getStatus()
        );
    }

    @Override
    public void delete(Integer id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Medicine not found: " + id);
        }
        repo.deleteById(id);
    }

    private Sort parseSort(String sort) {
        // default sort
        if (sort == null || sort.isBlank())
            return Sort.by(Sort.Direction.ASC, "genericName").and(Sort.by(Sort.Direction.ASC, "id"));


        // example: "brandName,asc"
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(dir, field);
    }
    @Override
    public AdminMedicineRowResponse getOne(Integer id) {
        var m = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));
        return mapRow(m);
    }

    @Override
    public List<AdminMedicineSuggestItem> suggest(String q) {
        if (q == null) return Collections.emptyList();
        q = q.trim();
        if (q.length() < 2) return Collections.emptyList();

        return repo
                .findTop10ByGenericNameStartingWithIgnoreCaseOrBrandNameStartingWithIgnoreCase(q, q)
                .stream()
                // dropdown shows only names, but we keep id hidden
                .map(m -> new AdminMedicineSuggestItem(m.getId(), m.getGenericName()))
                .distinct()
                .toList();
    }

}
