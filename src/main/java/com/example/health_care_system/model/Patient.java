package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("PATIENT")
public class Patient extends User {
    
    private LocalDate dateOfBirth;
    
    private String bloodType;
    
    private String address;
    
    private boolean active = true;
    
    @jakarta.persistence.Column(name = "qr_code", columnDefinition = "text")
    // Base64 encoded QR code image for patient identification (persisted as TEXT)
    private String qrCode;
    
    // Reference to the hospital where this patient is registered
    private String hospitalId;
    
    // Reference to medical records (lazy loaded) - keep transient for initial migration
    @Transient
    private List<MedicalRecord> medicalRecords = new ArrayList<>();
    
    // Reference to appointments (lazy loaded)
    @Transient
    private List<Appointment> appointments = new ArrayList<>();
}
