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

/**
 * Represents a temporary reservation of a time slot for 5 minutes
 * while a patient is in the confirmation process
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "time_slot_reservations")
public class TimeSlotReservation {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private String id;
    
    private String doctorId;
    
    private LocalDateTime slotDateTime;
    
    private String patientId;  // Patient who reserved the slot
    
    private String sessionId;  // Browser session ID for tracking
    
    private LocalDateTime createdAt;  // For application logic
    
    // Note: We're using scheduled cleanup instead of MongoDB TTL to avoid timing issues
    // The @Scheduled cleanup task in TimeSlotReservationService handles expiration
    
    private ReservationStatus status;
    
    public enum ReservationStatus {
        ACTIVE,      // Reservation is active
        CONFIRMED,   // Slot was booked
        CANCELLED,   // User cancelled before confirming
        EXPIRED      // 5 minutes passed without action
    }
}
