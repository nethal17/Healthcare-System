package com.example.health_care_system.controller;

import com.example.health_care_system.repository.TimeSlotReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin controller for managing reservations
 * TEMPORARY: Only for development/testing
 */
@Slf4j
@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class ReservationAdminController {
    
    private final TimeSlotReservationRepository reservationRepository;
    
    /**
     * Delete all reservations - USE ONLY FOR TESTING/CLEANUP
     */
    @PostMapping("/delete-all")
    public Map<String, Object> deleteAllReservations() {
        log.warn("Deleting all reservations - TESTING ONLY");
        try {
            long count = reservationRepository.count();
            reservationRepository.deleteAll();
            
            log.info("Deleted {} reservations", count);
            
            return Map.of(
                "success", true,
                "message", "All reservations deleted",
                "deletedCount", count
            );
        } catch (Exception e) {
            log.error("Error deleting reservations: {}", e.getMessage(), e);
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
        log.debug("Getting reservation status");
        try {
            long totalCount = reservationRepository.count();
            
            log.debug("Total reservations: {}", totalCount);
            
            return Map.of(
                "success", true,
                "totalReservations", totalCount
            );
        } catch (Exception e) {
            log.error("Error getting reservation status: {}", e.getMessage(), e);
            return Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            );
        }
    }
}
