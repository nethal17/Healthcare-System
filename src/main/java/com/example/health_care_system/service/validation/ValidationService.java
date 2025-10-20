package com.example.health_care_system.service.validation;

import com.example.health_care_system.dto.RegisterRequest;
import com.example.health_care_system.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service responsible for validation logic.
 * Follows Single Responsibility Principle - handles only validation.
 * Centralizes validation rules for better maintainability.
 */
@Service
public class ValidationService {
    
    /**
     * Validate registration request
     * 
     * @param request Registration data
     * @throws ValidationException if validation fails
     */
    public void validateRegistrationRequest(RegisterRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException("name", "Name is required");
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("email", "Email is required");
        }
        
        if (!isValidEmail(request.getEmail())) {
            throw new ValidationException("email", "Invalid email format");
        }
        
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidationException("password", "Password is required");
        }
        
        if (request.getPassword().length() < 6) {
            throw new ValidationException("password", "Password must be at least 6 characters");
        }
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("confirmPassword", "Passwords do not match");
        }
        
        if (request.getDateOfBirth() == null) {
            throw new ValidationException("dateOfBirth", "Date of birth is required");
        }
        
        if (request.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new ValidationException("dateOfBirth", "Date of birth cannot be in the future");
        }
        
        if (request.getContactNumber() == null || request.getContactNumber().trim().isEmpty()) {
            throw new ValidationException("contactNumber", "Contact number is required");
        }
    }
    
    /**
     * Validate appointment date
     * 
     * @param date Appointment date
     * @throws ValidationException if validation fails
     */
    public void validateAppointmentDate(LocalDate date) {
        if (date == null) {
            throw new ValidationException("date", "Appointment date is required");
        }
        
        if (date.isBefore(LocalDate.now())) {
            throw new ValidationException("date", "Cannot book appointments for past dates");
        }
    }
    
    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
