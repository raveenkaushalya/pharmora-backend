package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminMedicineSuggestItem {
    private Integer id;     // hidden id (frontend won’t show it)
    private String name;    // what you show in dropdown
}
