package com.example.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "pharmacy_inventory",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"pharmacy_id", "medicine_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyInventoryItem {
    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    @PrePersist
    public void prePersist() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Which pharmacy owns this inventory row
    @ManyToOne(optional = false)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    // Which medicine from medicine_master
    @ManyToOne(optional = false, fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "medicine_id")
    private MedicineMaster medicine;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
