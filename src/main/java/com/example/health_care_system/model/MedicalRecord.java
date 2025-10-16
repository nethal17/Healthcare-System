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
@Document(collection = "medical_records")
public class MedicalRecord {
    
    @Id
    private String id;
    
    private String patientId;  // References Patient.id (MongoDB ObjectId)
    
    private String patientName;
    
    private LocalDate recordDate;
    
    private String diagnosis;
    
    private String prescription;
    
    private String doctorId;  // References Doctor.id (MongoDB ObjectId)
    
    private String doctorName;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
