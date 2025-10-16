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
@Document(collection = "appointments")
public class Appointment {
    
    @Id
    private String id;
    
    private LocalDateTime appointmentDateTime;
    
    private String patientId;  // References Patient.id (MongoDB ObjectId)
    
    private String patientName;
    
    private String doctorId;  // References Doctor.id (MongoDB ObjectId)
    
    private String doctorName;
    
    private String purpose;
    
    private AppointmentStatus status;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public enum AppointmentStatus {
        SCHEDULED,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }
}
