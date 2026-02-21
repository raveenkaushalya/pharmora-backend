package com.example.backend.repository;

import com.example.backend.entity.InventoryActivity;
import com.example.backend.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryActivityRepository extends JpaRepository<InventoryActivity, Integer> {

    List<InventoryActivity> findTop20ByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
}
