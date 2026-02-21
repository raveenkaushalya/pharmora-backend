package com.example.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.InventoryActivityResponse;
import com.example.backend.entity.Pharmacy;
import com.example.backend.entity.User;
import com.example.backend.repository.InventoryActivityRepository;
import com.example.backend.repository.PharmacyRepository;
import com.example.backend.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/pharmacies/activity")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173",
        "https://raveenkaushalya.github.io",
        "https://raveenkaushalya.github.io/Medicine-Availability-Tracker/"
}, allowCredentials = "true")
public class PharmacyActivityController {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final InventoryActivityRepository activityRepository;

    public PharmacyActivityController(
            UserRepository userRepository,
            PharmacyRepository pharmacyRepository,
            InventoryActivityRepository activityRepository
    ) {
        this.userRepository = userRepository;
        this.pharmacyRepository = pharmacyRepository;
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

        List<InventoryActivityResponse> rows = activityRepository
                .findTop20ByPharmacyOrderByCreatedAtDesc(pharmacy)
                .stream()
                .map(a -> new InventoryActivityResponse(
                        a.getId(),
                        a.getAction(),
                        a.getMessage(),
                        a.getMedicine() != null ? a.getMedicine().getId() : null,
                        a.getMedicine() != null
                                ? (a.getMedicine().getGenericName() + " " + a.getMedicine().getDosage())
                                : null,
                        a.getCreatedAt()
                ))
                .toList();

        return new ApiResponse(true, "OK", rows);
    }
}
