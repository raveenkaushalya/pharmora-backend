package com.example.backend.repository;

import com.example.backend.entity.MedicineMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface MedicineMasterRepository extends JpaRepository<MedicineMaster, Integer>, JpaSpecificationExecutor<MedicineMaster> {

    Optional<MedicineMaster> findByRegNo(String regNo);

    // Autocomplete (top 10)
    List<MedicineMaster> findTop10ByGenericNameStartingWithIgnoreCaseOrBrandNameStartingWithIgnoreCase(
            String genericName,
            String brandName
    );
    @Query("select distinct m.manufacturer from MedicineMaster m where m.manufacturer is not null and m.manufacturer <> '' order by m.manufacturer")
    List<String> findDistinctManufacturers();

    @Query("select distinct m.brandName from MedicineMaster m where m.brandName is not null and m.brandName <> '' order by m.brandName")
    List<String> findDistinctBrandNames();

}
