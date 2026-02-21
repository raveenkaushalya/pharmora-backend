package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    // ✅ Security key stored in application.properties
    @Value("${app.admin.security-key}")
    private String adminSecurityKey;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    // ✅ LOGIN (requires username + password + securityKey)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest req,
                                             HttpServletRequest request,
                                             jakarta.servlet.http.HttpServletResponse response)
    {

        // 1) Check Security Key first
        if (req.getSecurityKey() == null || req.getSecurityKey().isBlank()) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Security key is required", null));
        }

        if (!req.getSecurityKey().equals(adminSecurityKey)) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Invalid security key", null));
        }

        // 2) Authenticate username/password (accept username OR email)
        String loginId = (req.getUsername() != null && !req.getUsername().isBlank())
                ? req.getUsername()
                : req.getEmail();

        if (loginId == null || loginId.isBlank()) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Username or Email is required", null));
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginId, req.getPassword())
        );

// ✅ create session FIRST
        request.getSession(true);

// ✅ explicitly save SecurityContext into session
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        var repo = new org.springframework.security.web.context.HttpSessionSecurityContextRepository();
        repo.saveContext(context, request, response);


        return ResponseEntity.ok(new ApiResponse(true, "Logged in successfully", auth.getName()));
    }

    // ✅ Check session
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Not logged in", null));
        }
        return ResponseEntity.ok(new ApiResponse(true, "OK", authentication.getName()));
    }

    // ✅ Logout (invalidate session)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();

        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new ApiResponse(true, "Logged out", null));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String email;
        private String password;
        private String securityKey;
    }
}
