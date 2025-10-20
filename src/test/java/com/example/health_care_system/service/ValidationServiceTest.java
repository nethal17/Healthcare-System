package com.example.health_care_system.service;

import com.example.health_care_system.dto.RegisterRequest;
import com.example.health_care_system.exception.ValidationException;
import com.example.health_care_system.service.validation.ValidationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    private final ValidationService service = new ValidationService();

    @Test
    void validateRegistrationRequest_valid_passes() {
        RegisterRequest r = new RegisterRequest();
        r.setName("Name");
        r.setEmail("user@example.com");
        r.setPassword("secret1");
        r.setConfirmPassword("secret1");
        r.setDateOfBirth(LocalDate.of(1990,1,1));
        r.setContactNumber("0771234567");

        assertDoesNotThrow(() -> service.validateRegistrationRequest(r));
    }

    @Test
    void validateRegistrationRequest_invalidEmail_throws() {
        RegisterRequest r = new RegisterRequest();
        r.setName("Name");
        r.setEmail("bad-email");
        r.setPassword("secret1");
        r.setConfirmPassword("secret1");
        r.setDateOfBirth(LocalDate.of(1990,1,1));
        r.setContactNumber("0771234567");

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validateRegistrationRequest(r));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void validateRegistrationRequest_passwordMismatch_throws() {
        RegisterRequest r = new RegisterRequest();
        r.setName("Name");
        r.setEmail("u@example.com");
        r.setPassword("123456");
        r.setConfirmPassword("123");
        r.setDateOfBirth(LocalDate.of(1990,1,1));
        r.setContactNumber("0771234567");

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validateRegistrationRequest(r));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void validateRegistrationRequest_futureDob_throws() {
        RegisterRequest r = new RegisterRequest();
        r.setName("Name");
        r.setEmail("u@example.com");
        r.setPassword("123456");
        r.setConfirmPassword("123456");
        r.setDateOfBirth(LocalDate.now().plusDays(1));
        r.setContactNumber("0771234567");

        ValidationException ex = assertThrows(ValidationException.class, () -> service.validateRegistrationRequest(r));
        assertTrue(ex.getMessage().toLowerCase().contains("date of birth") || ex.getMessage().toLowerCase().contains("date"));
    }

    @Test
    void validateAppointmentDate_nullOrPast_throws() {
        ValidationException ex1 = assertThrows(ValidationException.class, () -> service.validateAppointmentDate(null));
        assertTrue(ex1.getMessage().toLowerCase().contains("date"));

        ValidationException ex2 = assertThrows(ValidationException.class, () -> service.validateAppointmentDate(LocalDate.now().minusDays(1)));
        assertTrue(ex2.getMessage().toLowerCase().contains("past"));
    }

    @Test
    void validateRegistration_nullRequest_throws() {
        assertThrows(NullPointerException.class, () -> service.validateRegistrationRequest(null));
    }

    @Test
    void validateAppointmentDate_farFuture_allowedOrHandled() {
        // Ensure a far future date does not throw a past-date exception
        assertDoesNotThrow(() -> service.validateAppointmentDate(LocalDate.now().plusYears(1)));
    }
}

