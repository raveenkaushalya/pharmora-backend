package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pharmacies")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class PharmacyAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PharmacyAuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginRequest req, HttpServletRequest request) {

        if (req.username == null || req.username.isBlank())
            throw new RuntimeException("Email is required");

        if (req.password == null || req.password.isBlank())
            throw new RuntimeException("Password is required");

        User user = userRepository.findByUsername(req.username)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!"PHARMACY".equalsIgnoreCase(user.getRole()))
            throw new RuntimeException("Not a pharmacy account");

        if (!user.isEnabled())
            throw new RuntimeException("Account not enabled yet. Please set password using setup link.");

        if (user.getPassword() == null || !passwordEncoder.matches(req.password, user.getPassword()))
            throw new RuntimeException("Invalid email or password");

        // ✅ Session login (simple)
        request.getSession(true).setAttribute("USER_ID", user.getId());


        return new ApiResponse(true, "Pharmacy logged in", user.getUsername());
    }

    @PostMapping("/logout")
    public ApiResponse logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        return new ApiResponse(true, "Logged out", null);
    }

    @Data
    public static class LoginRequest {
        public String username;
        public String password;
    }
}
