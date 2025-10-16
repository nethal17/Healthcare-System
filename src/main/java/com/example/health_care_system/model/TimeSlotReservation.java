package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a temporary reservation of a time slot for 5 minutes
 * while a patient is in the confirmation process
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "time_slot_reservations")
@CompoundIndex(
    name = "unique_active_slot_idx",
    def = "{'doctorId': 1, 'slotDateTime': 1, 'status': 1}",
    unique = true,
    partialFilter = "{'status': 'ACTIVE'}"
)
public class TimeSlotReservation {
    
    @Id
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
