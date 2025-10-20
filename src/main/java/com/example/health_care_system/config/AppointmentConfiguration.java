package com.example.health_care_system.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;

/**
 * Configuration class for appointment-related constants.
 * Follows Open/Closed Principle - configuration can be changed without modifying code.
 * Eliminates magic numbers scattered throughout the codebase.
 */
@Configuration
@Getter
public class AppointmentConfiguration {
    
    @Value("${appointment.working.start:09:00}")
    private String workingStartTime;
    
    @Value("${appointment.working.end:17:00}")
    private String workingEndTime;
    
    @Value("${appointment.lunch.start:13:00}")
    private String lunchStartTime;
    
    @Value("${appointment.lunch.end:14:00}")
    private String lunchEndTime;
    
    @Value("${appointment.slot.duration:30}")
    private int slotDurationMinutes;
    
    @Value("${appointment.booking.advance.hours:1}")
    private int minimumAdvanceBookingHours;
    
    @Value("${appointment.booking.days.ahead:7}")
    private int maxDaysAhead;
    
    @Value("${appointment.reservation.timeout.minutes:15}")
    private int reservationTimeoutMinutes;
    
    /**
     * Get working start time as LocalTime object
     */
    public LocalTime getWorkingStart() {
        return LocalTime.parse(workingStartTime);
    }
    
    /**
     * Get working end time as LocalTime object
     */
    public LocalTime getWorkingEnd() {
        return LocalTime.parse(workingEndTime);
    }
    
    /**
     * Get lunch start time as LocalTime object
     */
    public LocalTime getLunchStart() {
        return LocalTime.parse(lunchStartTime);
    }
    
    /**
     * Get lunch end time as LocalTime object
     */
    public LocalTime getLunchEnd() {
        return LocalTime.parse(lunchEndTime);
    }
}
