package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "health_cards")
public class HealthCard {
    
    @Id
    private String id; // MongoDB ObjectId
    
    private String patientId; // Reference to Patient ID
    
    private String patientName;
    
    private String qrCode; // Base64 encoded QR code image
    
    private String status; // "ACTIVE" or "INACTIVE"
    
    private LocalDate createDate;
    
    private LocalDate expireDate;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
