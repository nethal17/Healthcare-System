package com.example.health_care_system.exception;

/**
 * Exception thrown when an appointment is not found.
 */
public class AppointmentNotFoundException extends ResourceNotFoundException {
    
    public AppointmentNotFoundException(String appointmentId) {
        super("Appointment", appointmentId);
    }
}
