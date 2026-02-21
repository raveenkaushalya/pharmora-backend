package com.example.backend.repository;

import com.example.backend.entity.Pharmacy;
import com.example.backend.entity.PharmacyLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PharmacyLocationRepository extends JpaRepository<PharmacyLocation, Integer> {
    Optional<PharmacyLocation> findByPharmacy(Pharmacy pharmacy);
}
