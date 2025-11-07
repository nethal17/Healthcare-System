package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "health_cards")
public class HealthCard {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private String id;
    
    private String patientId; // Reference to Patient ID
    
    private String patientName;

    @Column(name = "qr_code", columnDefinition = "text")
    // Persist QR code as TEXT to support long base64 data URIs
    private String qrCode;
    
    private String status; // "ACTIVE" or "INACTIVE"
    
    private LocalDate createDate;
    
    private LocalDate expireDate;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
