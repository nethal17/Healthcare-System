package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private String id;
    
    private LocalDateTime appointmentDateTime;
    
    private String patientId;  // References Patient.id
    
    private String patientName;
    
    private String doctorId;  // References Doctor.id
    
    private String doctorName;
    
    private String purpose;
    
    private AppointmentStatus status;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime actualCheckInTime; // Set when patient checks in
    private LocalDateTime actualCheckOutTime; // Set when patient leaves
    
    public enum AppointmentStatus {
        CONFIRMED,
        SCHEDULED,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }
}
