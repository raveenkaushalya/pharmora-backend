package com.example.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.request.AddInventoryRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PharmacyInventoryRowResponse;
import com.example.backend.entity.InventoryActivity;
import com.example.backend.entity.MedicineMaster;
import com.example.backend.entity.Pharmacy;
import com.example.backend.entity.PharmacyInventoryItem;
import com.example.backend.entity.User;
import com.example.backend.repository.InventoryActivityRepository;
import com.example.backend.repository.MedicineMasterRepository;
import com.example.backend.repository.PharmacyInventoryRepository;
import com.example.backend.repository.PharmacyRepository;
import com.example.backend.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pharmacies/inventory")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173",
        "https://raveenkaushalya.github.io",
        "https://raveenkaushalya.github.io/Medicine-Availability-Tracker/"
}, allowCredentials = "true")
public class PharmacyInventoryController {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineMasterRepository medicineMasterRepository;
    private final PharmacyInventoryRepository inventoryRepository;

    // ✅ ADD THIS
    private final InventoryActivityRepository activityRepository;

    // ✅ UPDATE CONSTRUCTOR (add activityRepository)
    public PharmacyInventoryController(
            UserRepository userRepository,
            PharmacyRepository pharmacyRepository,
            MedicineMasterRepository medicineMasterRepository,
            PharmacyInventoryRepository inventoryRepository,
            InventoryActivityRepository activityRepository
    ) {
        this.userRepository = userRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.medicineMasterRepository = medicineMasterRepository;
        this.inventoryRepository = inventoryRepository;
        this.activityRepository = activityRepository;
    }

    private User requireLoggedInUser(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) throw new RuntimeException("Not logged in");

        Integer userId = (Integer) session.getAttribute("USER_ID");
        if (userId == null) throw new RuntimeException("Not logged in");

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Pharmacy requireMyPharmacy(User user) {
        return pharmacyRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));
    }

    @GetMapping
    public ApiResponse list(HttpServletRequest request) {
        User user = requireLoggedInUser(request);
        Pharmacy pharmacy = requireMyPharmacy(user);

        List<PharmacyInventoryRowResponse> rows = inventoryRepository
                .findByPharmacyOrderByIdDesc(pharmacy)
                .stream()
                .map(i -> new PharmacyInventoryRowResponse(
                        i.getId(),
                        i.getMedicine().getId(),
                        i.getMedicine().getRegNo(),
                        i.getMedicine().getGenericName(),
                        i.getMedicine().getBrandName(),
                        i.getMedicine().getDosage(),
                        i.getMedicine().getManufacturer(),
                        i.getMedicine().getCountry(),
                        i.getStock(),
                        i.getPrice()
                ))
                .toList();

        return new ApiResponse(true, "OK", rows);
    }

    @PostMapping
    public ApiResponse add(@Valid @RequestBody AddInventoryRequest req, HttpServletRequest request) {
        User user = requireLoggedInUser(request);
        Pharmacy pharmacy = requireMyPharmacy(user);

        MedicineMaster med = medicineMasterRepository.findById(req.getMedicineId())
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        var existing = inventoryRepository.findByPharmacyIdAndMedicineId(pharmacy.getId(), med.getId());
        boolean wasUpdate = existing.isPresent();

        PharmacyInventoryItem item = existing.orElseGet(PharmacyInventoryItem::new);
        item.setPharmacy(pharmacy);
        item.setMedicine(med);
        item.setStock(req.getStock());
        item.setPrice(req.getPrice());

        inventoryRepository.save(item);

        // ✅ LOG ACTIVITY (Step 4)
        String medName =
                (med.getGenericName() != null ? med.getGenericName() : "") +
                        (med.getDosage() != null ? " " + med.getDosage() : "");

        InventoryActivity log = new InventoryActivity();
        log.setPharmacy(pharmacy);
        log.setMedicine(med);
        log.setAction(wasUpdate ? "UPDATED" : "ADDED");
        log.setMessage((wasUpdate ? "Updated" : "Added") + " inventory: " + medName +
                " | Stock: " + req.getStock() + " | Price: " + req.getPrice());

        activityRepository.save(log);

        return new ApiResponse(true, wasUpdate ? "Inventory updated" : "Inventory added", item.getId());
    }

    @PutMapping("/{id}")
    public ApiResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody AddInventoryRequest req,
            HttpServletRequest request
    ) {
        User user = requireLoggedInUser(request);
        Pharmacy pharmacy = requireMyPharmacy(user);

        PharmacyInventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Row not found"));

        if (!item.getPharmacy().getId().equals(pharmacy.getId()))
            throw new RuntimeException("Not allowed");

        // Only update price + stock (medicineId in body is ignored here)
        item.setStock(req.getStock());
        item.setPrice(req.getPrice());

        inventoryRepository.save(item);

        return new ApiResponse(true, "Inventory updated", null);
    }


    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Integer id, HttpServletRequest request) {
        User user = requireLoggedInUser(request);
        Pharmacy pharmacy = requireMyPharmacy(user);

        PharmacyInventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Row not found"));

        if (!item.getPharmacy().getId().equals(pharmacy.getId()))
            throw new RuntimeException("Not allowed");

        // ✅ LOG BEFORE DELETE
        MedicineMaster med = item.getMedicine();
        String medName =
                (med.getGenericName() != null ? med.getGenericName() : "") +
                        (med.getDosage() != null ? " " + med.getDosage() : "");

        InventoryActivity log = new InventoryActivity();
        log.setPharmacy(pharmacy);
        log.setMedicine(med);
        log.setAction("DELETED");
        log.setMessage("Deleted inventory item: " + medName);
        activityRepository.save(log);

        inventoryRepository.delete(item);

        return new ApiResponse(true, "Deleted", null);
    }
}
