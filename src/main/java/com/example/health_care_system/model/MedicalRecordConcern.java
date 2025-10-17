package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "medical_record_concerns")
public class MedicalRecordConcern {
    
    @Id
    private String id;
    
    private String medicalRecordId;  // References MedicalRecord.id
    
    private String patientId;  // References Patient.id
    
    private String patientName;
    
    private String patientEmail;
    
    private String concernText;
    
    private String status;  // PENDING, REPLIED
    
    private String replyText;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime repliedAt;
    
    private String repliedBy;  // Admin who replied
}
