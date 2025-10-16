package com.example.health_care_system.service;

import com.example.health_care_system.model.TimeSlotReservation;
import com.example.health_care_system.repository.TimeSlotReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TimeSlotReservationService {
    
    @Autowired
    private TimeSlotReservationRepository reservationRepository;
    
    private static final int RESERVATION_DURATION_MINUTES = 5;
    
    /**
     * Create a new reservation for a time slot
     * Returns the reservation ID if successful, null if slot is already reserved
     */
    @Transactional
    public TimeSlotReservation reserveTimeSlot(String doctorId, LocalDateTime slotDateTime, 
                                                String patientId, String sessionId) {
        // Check if slot is already reserved by someone else
        List<TimeSlotReservation> existingReservations = reservationRepository
            .findByDoctorIdAndSlotDateTimeAndStatus(
                doctorId, 
                slotDateTime, 
                TimeSlotReservation.ReservationStatus.ACTIVE
            );
        
        // If there's an active reservation by another user, slot is not available
        for (TimeSlotReservation reservation : existingReservations) {
            if (!reservation.getPatientId().equals(patientId)) {
                return null; // Slot is reserved by another user
            }
        }
        
        // Cancel any previous active reservation by this patient (they're selecting a new slot)
        cancelPatientActiveReservations(patientId);
        
        // Create new reservation
        TimeSlotReservation reservation = new TimeSlotReservation();
        reservation.setDoctorId(doctorId);
        reservation.setSlotDateTime(slotDateTime);
        reservation.setPatientId(patientId);
        reservation.setSessionId(sessionId);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setStatus(TimeSlotReservation.ReservationStatus.ACTIVE);
        
        return reservationRepository.save(reservation);
    }
    
    /**
     * Check if a time slot is currently reserved
     */
    public boolean isSlotReserved(String doctorId, LocalDateTime slotDateTime, String excludePatientId) {
        List<TimeSlotReservation> reservations = reservationRepository
            .findByDoctorIdAndSlotDateTimeAndStatus(
                doctorId, 
                slotDateTime, 
                TimeSlotReservation.ReservationStatus.ACTIVE
            );
        
        // Check if any reservation exists from a different patient
        return reservations.stream()
            .anyMatch(r -> !r.getPatientId().equals(excludePatientId));
    }
    
    /**
     * Confirm a reservation (when appointment is booked)
     */
    @Transactional
    public void confirmReservation(String patientId, String sessionId) {
        Optional<TimeSlotReservation> reservationOpt = reservationRepository
            .findByPatientIdAndSessionIdAndStatus(
                patientId, 
                sessionId, 
                TimeSlotReservation.ReservationStatus.ACTIVE
            );
        
        if (reservationOpt.isPresent()) {
            TimeSlotReservation reservation = reservationOpt.get();
            reservation.setStatus(TimeSlotReservation.ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
            System.out.println("Reservation confirmed (by session): " + reservation.getId());
        } else {
            // Fallback: find by patient ID only
            List<TimeSlotReservation> patientReservations = reservationRepository.findByPatientIdAndStatus(
                patientId,
                TimeSlotReservation.ReservationStatus.ACTIVE
            );
            
            if (!patientReservations.isEmpty()) {
                TimeSlotReservation reservation = patientReservations.get(0);
                reservation.setStatus(TimeSlotReservation.ReservationStatus.CONFIRMED);
                reservationRepository.save(reservation);
                System.out.println("Reservation confirmed (by patient ID fallback): " + reservation.getId());
            } else {
                System.out.println("WARNING: No reservation to confirm for patient: " + patientId);
            }
        }
    }
    
    /**
     * Cancel a specific reservation (when user cancels or selects different slot)
     */
    @Transactional
    public void cancelReservation(String patientId, String sessionId) {
        Optional<TimeSlotReservation> reservationOpt = reservationRepository
            .findByPatientIdAndSessionIdAndStatus(
                patientId, 
                sessionId, 
                TimeSlotReservation.ReservationStatus.ACTIVE
            );
        
        if (reservationOpt.isPresent()) {
            TimeSlotReservation reservation = reservationOpt.get();
            reservation.setStatus(TimeSlotReservation.ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
        }
    }
    
    /**
     * Cancel all active reservations for a patient
     */
    @Transactional
    public void cancelPatientActiveReservations(String patientId) {
        List<TimeSlotReservation> activeReservations = reservationRepository
            .findByPatientIdAndStatus(
                patientId, 
                TimeSlotReservation.ReservationStatus.ACTIVE
            );
        
        for (TimeSlotReservation reservation : activeReservations) {
            reservation.setStatus(TimeSlotReservation.ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
        }
    }
    
    /**
     * Get active reservation for a patient
     */
    public Optional<TimeSlotReservation> getActiveReservation(String patientId, String sessionId) {
        return reservationRepository.findByPatientIdAndSessionIdAndStatus(
            patientId, 
            sessionId, 
            TimeSlotReservation.ReservationStatus.ACTIVE
        );
    }
    
        /**
     * Scheduled task to clean up expired reservations
     * Runs every 60 seconds
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredReservations() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(RESERVATION_DURATION_MINUTES);
        
        List<TimeSlotReservation> expiredReservations = reservationRepository
            .findByStatusAndCreatedAtBefore(
                TimeSlotReservation.ReservationStatus.ACTIVE,
                expiryTime
            );
        
        for (TimeSlotReservation reservation : expiredReservations) {
            reservation.setStatus(TimeSlotReservation.ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
        }
        
        if (!expiredReservations.isEmpty()) {
            System.out.println("Cleaned up " + expiredReservations.size() + " expired reservations");
        }
    }
    
    /**
     * Check if a reservation is still valid (not expired)
     */
    public boolean isReservationValid(String patientId, String sessionId) {
        Optional<TimeSlotReservation> reservationOpt = getActiveReservation(patientId, sessionId);
        
        if (reservationOpt.isEmpty()) {
            // Try to find by patient ID only as fallback (in case session changed)
            List<TimeSlotReservation> patientReservations = reservationRepository.findByPatientIdAndStatus(
                patientId,
                TimeSlotReservation.ReservationStatus.ACTIVE
            );
            
            if (!patientReservations.isEmpty()) {
                // Use the most recent reservation
                TimeSlotReservation reservation = patientReservations.get(0);
                LocalDateTime expiryTime = reservation.getCreatedAt().plusMinutes(RESERVATION_DURATION_MINUTES);
                return LocalDateTime.now().isBefore(expiryTime);
            }
            
            return false;
        }
        
        TimeSlotReservation reservation = reservationOpt.get();
        LocalDateTime expiryTime = reservation.getCreatedAt().plusMinutes(RESERVATION_DURATION_MINUTES);
        return LocalDateTime.now().isBefore(expiryTime);
    }
    
    /**
     * Get remaining time in seconds for a reservation
     */
    public long getRemainingSeconds(String patientId, String sessionId) {
        Optional<TimeSlotReservation> reservationOpt = getActiveReservation(patientId, sessionId);
        
        if (reservationOpt.isEmpty()) {
            // Try to find by patient ID only as fallback
            List<TimeSlotReservation> patientReservations = reservationRepository.findByPatientIdAndStatus(
                patientId,
                TimeSlotReservation.ReservationStatus.ACTIVE
            );
            
            if (!patientReservations.isEmpty()) {
                TimeSlotReservation reservation = patientReservations.get(0);
                LocalDateTime expiryTime = reservation.getCreatedAt().plusMinutes(RESERVATION_DURATION_MINUTES);
                long remainingSeconds = java.time.Duration.between(LocalDateTime.now(), expiryTime).getSeconds();
                return Math.max(0, remainingSeconds);
            }
            
            return 0;
        }
        
        TimeSlotReservation reservation = reservationOpt.get();
        LocalDateTime expiryTime = reservation.getCreatedAt().plusMinutes(RESERVATION_DURATION_MINUTES);
        long remainingSeconds = java.time.Duration.between(LocalDateTime.now(), expiryTime).getSeconds();
        return Math.max(0, remainingSeconds);
    }
}
