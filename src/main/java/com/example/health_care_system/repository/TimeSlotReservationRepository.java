package com.example.health_care_system.repository;

import com.example.health_care_system.model.TimeSlotReservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotReservationRepository extends MongoRepository<TimeSlotReservation, String> {
    
    /**
     * Find active reservations for a specific doctor and time slot
     */
    List<TimeSlotReservation> findByDoctorIdAndSlotDateTimeAndStatus(
        String doctorId, 
        LocalDateTime slotDateTime, 
        TimeSlotReservation.ReservationStatus status
    );
    
    /**
     * Find active reservations for a specific doctor
     */
    List<TimeSlotReservation> findByDoctorIdAndStatus(
        String doctorId,
        TimeSlotReservation.ReservationStatus status
    );
    
    /**
     * Find reservation by patient and session
     */
    Optional<TimeSlotReservation> findByPatientIdAndSessionIdAndStatus(
        String patientId, 
        String sessionId, 
        TimeSlotReservation.ReservationStatus status
    );
    
    /**
     * Find all active reservations for a patient
     */
    List<TimeSlotReservation> findByPatientIdAndStatus(
        String patientId, 
        TimeSlotReservation.ReservationStatus status
    );
    
    /**
     * Find active reservations created before a specific time (for cleanup)
     */
    List<TimeSlotReservation> findByStatusAndCreatedAtBefore(
        TimeSlotReservation.ReservationStatus status,
        LocalDateTime dateTime
    );
    
    /**
     * Find reservation by session ID
     */
    Optional<TimeSlotReservation> findBySessionIdAndStatus(
        String sessionId,
        TimeSlotReservation.ReservationStatus status
    );
}
