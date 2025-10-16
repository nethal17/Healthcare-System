package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {
    
    @Id
    private String id;
    
    private String appointmentId;  // References Appointment.id
    
    // Patient/User Information
    private String patientId;  // User ID - References Patient.id
    private String patientName;  // User Name
    
    // Hospital Information
    private String hospitalId;  // References Hospital.id
    private String hospitalName;  // Hospital Name
    
    // Doctor Information
    private String doctorId;  // References Doctor.id
    private String doctorName;  // Doctor Name
    private String doctorSpecialization;  // Doctor's Specialization
    
    // Payment Information
    private BigDecimal amount;  // Payment Amount
    
    private PaymentMethod paymentMethod;
    
    private PaymentStatus status;
    
    private String transactionId;  // For card payments (Stripe session/transaction ID)
    
    private String insuranceProvider;  // For insurance payments
    
    private String insurancePolicyNumber;  // For insurance payments
    
    private LocalDateTime paymentDate;  // Payment Created Date and Time
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public enum PaymentMethod {
        CASH,
        CARD,
        INSURANCE
    }
    
    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}
