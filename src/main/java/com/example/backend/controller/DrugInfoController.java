package com.example.backend.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class DrugInfoController {
    @GetMapping("/api/drug-info")
    public ResponseEntity<?> getDrugInfo(@RequestParam String name) {
        if (name == null || name.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Missing drug name");
            return ResponseEntity.badRequest().body(error);
        }
        try {
            String url = UriComponentsBuilder.fromUriString("https://api.fda.gov/drug/label.json")
                    .queryParam("search", String.format("openfda.brand_name:\"%s\"+openfda.generic_name:\"%s\"", name, name))
                    .queryParam("limit", 1)
                    .build().toUriString();
            RestTemplate restTemplate = new RestTemplate();
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            List<?> results = null;
            if (response != null && response.get("results") instanceof List) {
                results = (List<?>) response.get("results");
            }
            if (results == null || results.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No information found for this drug.");
                return ResponseEntity.status(404).body(error);
            }
            Map<?, ?> result = (Map<?, ?>) results.get(0);
            Map<?, ?> openfda = null;
            if (result.get("openfda") instanceof Map) {
                openfda = (Map<?, ?>) result.get("openfda");
            } else {
                openfda = new HashMap<>();
            }
            Map<String, Object> mapped = new HashMap<>();
            mapped.put("name", getFirst(openfda.get("brand_name"), getFirst(openfda.get("generic_name"), "")));
            mapped.put("genericName", getFirst(openfda.get("generic_name"), ""));
            mapped.put("usages", getListOrEmpty(result.get("indications_and_usage")));
            Map<String, Object> sideEffects = new HashMap<>();
            sideEffects.put("common", getListOrEmpty(result.get("adverse_reactions")));
            sideEffects.put("serious", getListOrEmpty(result.get("warnings_and_cautions")));
            mapped.put("sideEffects", sideEffects);
            mapped.put("precautions", getListOrEmpty(result.get("precautions")));
            mapped.put("interactions", getListOrEmpty(result.get("drug_interactions")));
            mapped.put("dosageInfo", joinList(result.get("dosage_and_administration")));
            mapped.put("howItWorks", joinList(result.get("clinical_pharmacology")));
            return ResponseEntity.ok(mapped);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch drug info");
            error.put("details", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    private List<?> getListOrEmpty(Object obj) {
        if (obj instanceof List) {
            return (List<?>) obj;
        }
        return Collections.emptyList();
    }

    private String getFirst(Object obj, String defaultVal) {
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            if (!list.isEmpty()) {
                Object val = list.get(0);
                return val != null ? val.toString() : defaultVal;
            }
        }
        return defaultVal;
    }

    private String joinList(Object obj) {
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            if (!list.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Object o : list) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(o.toString());
                }
                return sb.toString();
            }
        }
        return "";
    }
}