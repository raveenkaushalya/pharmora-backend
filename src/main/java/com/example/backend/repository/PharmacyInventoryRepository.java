package com.example.backend.repository;

import com.example.backend.entity.Pharmacy;
import com.example.backend.entity.PharmacyInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PharmacyInventoryRepository extends JpaRepository<PharmacyInventoryItem, Integer> {

    List<PharmacyInventoryItem> findByPharmacyOrderByIdDesc(Pharmacy pharmacy);

    Optional<PharmacyInventoryItem> findByPharmacyIdAndMedicineId(Integer pharmacyId, Integer medicineId);
}
