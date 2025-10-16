package com.example.health_care_system.controller;

import com.example.health_care_system.repository.TimeSlotReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin controller for managing reservations
 * TEMPORARY: Only for development/testing
 */
@RestController
@RequestMapping("/admin/reservations")
public class ReservationAdminController {
    
    @Autowired
    private TimeSlotReservationRepository reservationRepository;
    
    /**
     * Delete all reservations - USE ONLY FOR TESTING/CLEANUP
     */
    @PostMapping("/delete-all")
    public Map<String, Object> deleteAllReservations() {
        try {
            long count = reservationRepository.count();
            reservationRepository.deleteAll();
            
            return Map.of(
                "success", true,
                "message", "All reservations deleted",
                "deletedCount", count
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            );
        }
    }
    
    /**
     * Get count of all reservations by status
     */
    @PostMapping("/status")
    public Map<String, Object> getReservationStatus() {
        try {
            long totalCount = reservationRepository.count();
            
            return Map.of(
                "success", true,
                "totalReservations", totalCount
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            );
        }
    }
}
