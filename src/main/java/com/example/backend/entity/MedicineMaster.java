package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "medicine_master",
        indexes = {
                @Index(name = "idx_medicine_generic", columnList = "genericName"),
                @Index(name = "idx_medicine_brand", columnList = "brandName"),
                @Index(name = "idx_medicine_regno", columnList = "regNo")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineMaster {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        private String genericName;
        private String brandName;
        private String manufacturer;
        private String country;
        @Column(nullable = false, unique = true, length = 50)
        private String regNo;
                private String dosage;

                // CatalogStatus enum for medicine status
                private CatalogStatus status;
        
                public CatalogStatus getStatus() {
                    return status;
                }
        
                public void setStatus(CatalogStatus status) {
                    this.status = status;
                }
}
