package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.backend.dto.request.SetPasswordRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.service.PasswordSetupService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pharmacies")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:3000",
    "https://raveenkaushalya.github.io",
    "https://raveenkaushalya.github.io/Medicine-Availability-Tracker/"
}, allowCredentials = "true")

public class PharmacyPasswordController {

    @Value("${emailjs.service-id}")
    private String emailJsServiceId;
    @Value("${emailjs.template-id}")
    private String emailJsTemplateId;
    @Value("${emailjs.public-key}")
    private String emailJsPublicKey;
    @Value("${emailjs.private-key}")
    private String emailJsPrivateKey;
    @Value("${app.frontend.base-url:https://raveenkaushalya.github.io/Medicine-Availability-Tracker/}")
    private String frontendBaseUrl;

    private final PasswordSetupService passwordSetupService;

    public PharmacyPasswordController(PasswordSetupService passwordSetupService) {
        this.passwordSetupService = passwordSetupService;
    }
    @PostMapping("/forgot-password")
    public ApiResponse forgotPassword(@RequestBody ForgotPasswordRequest req) {
        String email = req.getEmail();
        String token = passwordSetupService.generateResetToken(email);
        String resetLink = frontendBaseUrl + "/#/pharmacy/reset-password?token=" + token;
        // Send email using EmailJS REST API
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.emailjs.com/api/v1.0/email/send";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // No Authorization header

            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("service_id", emailJsServiceId);
            body.put("template_id", emailJsTemplateId);
            body.put("user_id", emailJsPublicKey); // required public key
            body.put("accessToken", emailJsPrivateKey); // private key as accessToken

            java.util.Map<String, String> templateParams = new java.util.HashMap<>();
            templateParams.put("to_email", email);
            templateParams.put("reset_link", resetLink);
            body.put("template_params", templateParams);

            System.out.println("[EmailJS] Payload: " + body);
            HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(body, headers);
            var response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("[EmailJS] Response: " + response.getStatusCode() + " - " + response.getBody());
        } catch (org.springframework.web.client.RestClientException e) {
            return new ApiResponse(false, "Failed to send reset email: " + e.getMessage(), null);
        } catch (RuntimeException e) {
            return new ApiResponse(false, "Failed to send reset email: " + e.getMessage(), null);
        }
        return new ApiResponse(true, "Reset email sent", null);
    }

    public static class ForgotPasswordRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @PostMapping("/set-password")
    public ApiResponse setPassword(@Valid @RequestBody SetPasswordRequest req) {
        passwordSetupService.setPassword(req);
        return new ApiResponse(true, "Password set successfully. You can now login.", null);
    }
}
