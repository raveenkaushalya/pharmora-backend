package com.example.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePharmacyMeRequest {


    @Size(max = 100)
    private String contactFullName;

    @Size(max = 100)
    private String contactTitle;

    @Size(max = 30)
    private String contactPhone;

    @Size(max = 30)
    private String telephone;

    @Size(max = 2000)
    private String aboutPharmacy;

    private String openingHoursJson;

    @Size(max = 200)
    private String address;

    @Size(max = 200)
    private String streetAddress;

    @Size(max = 30)
    private String city;

    @Size(max = 30)
    private String state;

    @Size(max = 10)
    private String zipCode;

    @Size(max = 30)
    private String country;

    private Double latitude;
    private Double longitude;

}
