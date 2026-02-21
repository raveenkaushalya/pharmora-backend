package com.example.backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.request.UpdatePharmacyMeRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PharmacyMeResponse;
import com.example.backend.entity.Pharmacy;
import com.example.backend.entity.PharmacyLocation;
import com.example.backend.entity.User;
import com.example.backend.repository.PharmacyLocationRepository;
import com.example.backend.repository.PharmacyRepository;
import com.example.backend.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/pharmacies")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class PharmacyMeController {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyLocationRepository pharmacyLocationRepository;
    private final UserRepository userRepository;

    public PharmacyMeController(
            PharmacyRepository pharmacyRepository,
            PharmacyLocationRepository pharmacyLocationRepository,
            UserRepository userRepository
    ) {
        this.pharmacyRepository = pharmacyRepository;
        this.pharmacyLocationRepository = pharmacyLocationRepository;
        this.userRepository = userRepository;
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

    private PharmacyMeResponse toResponse(Pharmacy p, PharmacyLocation loc) {
        return PharmacyMeResponse.builder()
                .id(p.getId())
                .legalEntityName(p.getLegalEntityName())
                .tradeName(p.getTradeName())
                .nmraLicense(p.getNmraLicense())
                .businessRegNo(p.getBusinessRegNo())
                .address(p.getAddressInSriLanka())
                .telephone(p.getTelephone())
                .email(p.getEmail())
                .entityType(p.getEntityType())
                .contactFullName(p.getContactFullName())
                .contactTitle(p.getContactTitle())
                .contactPhone(p.getContactPhone())
                .aboutPharmacy(p.getAboutPharmacy())
                .openingHoursJson(p.getOpeningHoursJson())
                .streetAddress(loc != null ? loc.getStreetAddress() : null)
                .city(loc != null ? loc.getCity() : null)
                .state(loc != null ? loc.getState() : null)
                .zipCode(loc != null ? loc.getZipCode() : null)
                .country(loc != null ? loc.getCountry() : null)
                .latitude(loc != null ? loc.getLatitude() : null)
                .longitude(loc != null ? loc.getLongitude() : null)
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .build();
    }

    @GetMapping("/me")
    public ApiResponse me(HttpServletRequest request) {
        User user = requireLoggedInUser(request);
        Pharmacy pharmacy = requireMyPharmacy(user);

        PharmacyLocation loc = pharmacyLocationRepository.findByPharmacy(pharmacy).orElse(null);
        return new ApiResponse(true, "OK", toResponse(pharmacy, loc));
    }

    @PatchMapping("/me")
    public ApiResponse updateMe(@RequestBody UpdatePharmacyMeRequest req, HttpServletRequest request) {
        User user = requireLoggedInUser(request);
        Pharmacy pharmacy = requireMyPharmacy(user);


        // update pharmacy editable fields
        if (req.getContactFullName() != null) pharmacy.setContactFullName(req.getContactFullName());
        if (req.getContactTitle() != null) pharmacy.setContactTitle(req.getContactTitle());
        if (req.getContactPhone() != null) pharmacy.setContactPhone(req.getContactPhone());
        if (req.getTelephone() != null) pharmacy.setTelephone(req.getTelephone());
        if (req.getAboutPharmacy() != null) pharmacy.setAboutPharmacy(req.getAboutPharmacy());
        if (req.getOpeningHoursJson() != null) pharmacy.setOpeningHoursJson(req.getOpeningHoursJson());
        // PATCH: update addressInSriLanka if address is provided
        if (req.getAddress() != null) pharmacy.setAddressInSriLanka(req.getAddress());

        pharmacyRepository.save(pharmacy);

        // update/create location row if any location field is provided
        boolean locationTouched =
                req.getStreetAddress() != null || req.getCity() != null || req.getState() != null ||
                        req.getZipCode() != null || req.getCountry() != null ||
                        req.getLatitude() != null || req.getLongitude() != null;

        PharmacyLocation loc = pharmacyLocationRepository.findByPharmacy(pharmacy).orElse(null);

        if (locationTouched) {
            if (loc == null) {
                loc = PharmacyLocation.builder().pharmacy(pharmacy).build();
            }
            if (req.getStreetAddress() != null) loc.setStreetAddress(req.getStreetAddress());
            if (req.getCity() != null) loc.setCity(req.getCity());
            if (req.getState() != null) loc.setState(req.getState());
            if (req.getZipCode() != null) loc.setZipCode(req.getZipCode());
            if (req.getCountry() != null) loc.setCountry(req.getCountry());
            if (req.getLatitude() != null) loc.setLatitude(req.getLatitude());
            if (req.getLongitude() != null) loc.setLongitude(req.getLongitude());

            pharmacyLocationRepository.save(loc);
        }

        PharmacyLocation latestLoc = pharmacyLocationRepository.findByPharmacy(pharmacy).orElse(null);
        return new ApiResponse(true, "Profile updated", toResponse(pharmacy, latestLoc));
    }
}
