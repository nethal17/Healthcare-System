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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
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
