package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "users")
public class Patient extends User {
    
    private LocalDate dateOfBirth;
    
    private String address;
    
    private boolean active = true;
    
    private String qrCode; // Base64 encoded QR code image for patient identification
    
    // Reference to the hospital where this patient is registered
    private String hospitalId;
    
    // Reference to medical records (lazy loaded)
    @DBRef(lazy = true)
    private List<MedicalRecord> medicalRecords = new ArrayList<>();
    
    // Reference to appointments (lazy loaded)
    @DBRef(lazy = true)
    private List<Appointment> appointments = new ArrayList<>();
}
