package com.example.backend.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.backend.dto.request.PharmacyRegisterRequest;
import com.example.backend.dto.response.PharmacyApproveResponse;
import com.example.backend.dto.response.PharmacyRowResponse;
import com.example.backend.entity.PasswordSetupToken;
import com.example.backend.entity.Pharmacy;
import com.example.backend.entity.PharmacyStatus;
import com.example.backend.entity.User;
import com.example.backend.repository.PasswordSetupTokenRepository;
import com.example.backend.repository.PharmacyRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.PharmacyService;

@Service
public class PharmacyServiceImpl implements PharmacyService {
    @Override
    public long countAll() {
        return pharmacyRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        if (status == null || status.equalsIgnoreCase("ALL")) {
            return pharmacyRepository.count();
        }
        return pharmacyRepository.countByStatus(PharmacyStatus.valueOf(status.toUpperCase()));
    }

    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final PasswordSetupTokenRepository passwordSetupTokenRepository;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${app.pharmacy.password-setup-token-expiry-minutes:60}")
    private long tokenExpiryMinutes;

    public PharmacyServiceImpl(
            PharmacyRepository pharmacyRepository,
            UserRepository userRepository,
            PasswordSetupTokenRepository passwordSetupTokenRepository
    ) {
        this.pharmacyRepository = pharmacyRepository;
        this.userRepository = userRepository;
        this.passwordSetupTokenRepository = passwordSetupTokenRepository;
    }

    // ✅ Register pharmacy (PENDING)
    @Override
    public Integer register(PharmacyRegisterRequest request) {
        if (pharmacyRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        if (pharmacyRepository.existsByBusinessRegNo(request.getBusinessRegNo())) {
            throw new RuntimeException("Business Registration Number already exists!");
        }

        if (pharmacyRepository.existsByNmraLicense(request.getNmraLicense())) {
            throw new RuntimeException("NMRA License already exists!");
        }

        if (!request.isAgreedToDeclaration()) {
            throw new RuntimeException("Declaration must be accepted!");
        }

        Pharmacy pharmacy = new Pharmacy();

        // business
        pharmacy.setLegalEntityName(request.getLegalEntityName());
        pharmacy.setTradeName(request.getTradeName());
        pharmacy.setNmraLicense(request.getNmraLicense());
        pharmacy.setBusinessRegNo(request.getBusinessRegNo());
        pharmacy.setAddressInSriLanka(request.getAddressInSriLanka());
        pharmacy.setTelephone(request.getTelephone());
        pharmacy.setEmail(request.getEmail());
        pharmacy.setEntityType(request.getEntityType());

        // contact
        pharmacy.setContactFullName(request.getContactFullName());
        pharmacy.setContactTitle(request.getContactTitle());
        pharmacy.setContactPhone(request.getContactPhone());
        pharmacy.setContactEmail(request.getContactEmail());

        // declaration
        pharmacy.setDeclarationDate(request.getDeclarationDate());
        pharmacy.setAgreedToDeclaration(request.isAgreedToDeclaration());

        // defaults
        pharmacy.setStatus(PharmacyStatus.PENDING);
        pharmacy.setCreatedAt(LocalDateTime.now());

        return pharmacyRepository.save(pharmacy).getId();
    }

    // ✅ Admin list with pagination + filters
    @Override
    public Page<PharmacyRowResponse> getPharmaciesForAdmin(String status, String q, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        boolean hasQ = q != null && !q.isBlank();
        boolean hasStatus = status != null && !status.equalsIgnoreCase("ALL");

        Page<Pharmacy> result;

        if (hasStatus && status != null) {
            PharmacyStatus st = PharmacyStatus.valueOf(status.toUpperCase());
            result = hasQ
                ? pharmacyRepository.findByStatusAndLegalEntityNameContainingIgnoreCase(st, q, pageable)
                : pharmacyRepository.findByStatus(st, pageable);
        } else {
            result = hasQ
                ? pharmacyRepository.findByLegalEntityNameContainingIgnoreCase(q, pageable)
                : pharmacyRepository.findAll(pageable);
        }

        return result.map(this::toRowResponse);
    }

    // ✅ Approve pharmacy (Option A): APPROVED + create user (disabled) + setup token + return setup link
    @Override
    public PharmacyApproveResponse approvePharmacy(Integer pharmacyId) {

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found!"));

        if (pharmacy.getStatus() != PharmacyStatus.PENDING) {
            throw new RuntimeException("Only PENDING pharmacies can be approved!");
        }

        // Update status
        pharmacy.setStatus(PharmacyStatus.APPROVED);

        // Username must be pharmacy email
        String username = pharmacy.getEmail();
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Pharmacy email is required to create login");
        }

        // Create user only once
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("A user with this pharmacy email already exists!");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(null);       // password set later via token
        user.setRole("PHARMACY");
        user.setPharmacy(pharmacy);
        user.setEnabled(false);       // enable after password set

        user = userRepository.save(user);
        pharmacyRepository.save(pharmacy);

        // Create one-time setup token
        String rawToken = generateRawToken();
        String tokenHash = sha256Hex(rawToken);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(tokenExpiryMinutes);

        PasswordSetupToken token = new PasswordSetupToken();
        token.setUser(user);
        token.setTokenHash(tokenHash);
        token.setCreatedAt(now);
        token.setExpiresAt(expiresAt);
        token.setUsedAt(null);

        passwordSetupTokenRepository.save(token);

        // Setup link (admin copies and sends manually for now)
        String setupLink = frontendBaseUrl + "/pharmacy/set-password?token=" + rawToken;

        return new PharmacyApproveResponse(username, setupLink, expiresAt);
    }

    // ✅ Reject pharmacy
    @Override
    public void rejectPharmacy(Integer pharmacyId, String reason) {

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found!"));

        if (pharmacy.getStatus() != PharmacyStatus.PENDING) {
            throw new RuntimeException("Only PENDING pharmacies can be rejected!");
        }

        pharmacy.setStatus(PharmacyStatus.REJECTED);
        pharmacy.setRejectionReason(reason);

        pharmacyRepository.save(pharmacy);
    }

    @Override
    public Object getPharmaciesByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Pharmacy> result;

        if (status == null || status.equalsIgnoreCase("ALL")) {
            result = pharmacyRepository.findAll(pageable);
        } else {
            PharmacyStatus st = PharmacyStatus.valueOf(status.toUpperCase());
            result = pharmacyRepository.findByStatus(st, pageable);
        }

        return result.map(this::toRowResponse);
    }

    // ===== Helpers =====

    private PharmacyRowResponse toRowResponse(Pharmacy p) {
        return new PharmacyRowResponse(
                p.getId(),
                p.getLegalEntityName(),
                p.getTradeName(),
                p.getNmraLicense(),
                p.getBusinessRegNo(),
                p.getAddressInSriLanka(),
                p.getTelephone(),
                p.getEmail(),
                p.getEntityType(),
                p.getContactFullName(),
                p.getContactTitle(),
                p.getContactPhone(),
                p.getContactEmail(),
                p.getDeclarationDate(),
                p.isAgreedToDeclaration(),
                p.getStatus().name(),
                p.getRejectionReason(),
                p.getCreatedAt()
        );
    }


    private String generateRawToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
