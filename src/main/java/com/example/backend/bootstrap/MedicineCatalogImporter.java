package com.example.backend.bootstrap;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MedicineMaster;
import com.example.backend.repository.MedicineMasterRepository;
import com.opencsv.CSVReader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MedicineCatalogImporter implements CommandLineRunner {

    private final MedicineMasterRepository medicineRepo;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // If already imported once, skip
        long count = medicineRepo.count();
        if (count > 0) {
            System.out.println("medicine_master already has data. Skipping CSV import.");
            return;
        }

        // Track regNos we already saw (also catches duplicates inside CSV)
        Set<String> seenRegNos = new HashSet<>();

        var resource = new ClassPathResource("data/medicine_master.csv");
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {
            String[] header = csvReader.readNext();
            if (header == null) {
                System.out.println("CSV header is missing. Import aborted.");
                return;
            }
            List<MedicineMaster> batch = new ArrayList<>(100);
            int skippedDuplicates = 0;
            int importedCount = 0;
            String[] c;
            while ((c = csvReader.readNext()) != null) {
                if (c.length < 6) continue; // skip incomplete rows
                String genericName = clean(c, 0);
                String brandName = clean(c, 1);
                String manufacturer = clean(c, 2);
                String country = clean(c, 3);
                String regNo = clean(c, 4);
                if (regNo != null && regNo.length() > 50) {
                    System.out.println("⚠️ regNo truncated: " + regNo);
                    regNo = regNo.substring(0, 50);
                }
                String dosage = clean(c, 5);
                if (regNo == null) continue;
                if (!seenRegNos.add(regNo)) {
                    skippedDuplicates++;
                    continue;
                }
                MedicineMaster m = new MedicineMaster();
                m.setGenericName(genericName);
                m.setBrandName(brandName);
                m.setManufacturer(manufacturer);
                m.setCountry(country);
                m.setRegNo(regNo);
                m.setDosage(dosage);
                m.setStatus(com.example.backend.entity.CatalogStatus.ACTIVE); // Ensure status is ACTIVE
                batch.add(m);

                // Reduced batch size to 100 to avoid connection overload
                if (batch.size() == 100) {
                    medicineRepo.saveAll(batch);
                    importedCount += batch.size();
                    batch.clear();
                    System.out.println("📥 Imported: " + importedCount + " medicines...");
                }
            }
            if (!batch.isEmpty()) {
                medicineRepo.saveAll(batch);
                importedCount += batch.size();
            }
            System.out.println("Imported medicine_master successfully!");
            System.out.println("Skipped duplicate reg_no rows: " + skippedDuplicates);
            System.out.println("Final imported count: " + importedCount);
        }
    }

    private static String clean(String[] c, int i) {
        if (c.length <= i || c[i] == null) return null;
        String s = c[i].trim();
        if (s.isEmpty()) return null;

        // remove surrounding quotes
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }

        return s.trim();
    }

    private static LocalDate parseDate(String s) {
        try {
            if (s == null) return null;
            return LocalDate.parse(s); // yyyy-mm-dd
        } catch (Exception e) {
            return null;
        }
    }
}