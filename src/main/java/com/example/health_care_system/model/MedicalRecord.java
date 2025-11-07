package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medical_records")
public class MedicalRecord {
    
    @Id
    private String id;
    
    private String patientId;  // References Patient.id
    
    private String patientName;
    
    private LocalDate recordDate;
    
    private String diagnosis;
    
    private String prescription;
    
    private String doctorId;  // References Doctor.id
    
    private String doctorName;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
