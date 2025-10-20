package com.example.health_care_system.service;

import com.example.health_care_system.exception.UserNotFoundException;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.User;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.repository.StaffRepository;
import com.example.health_care_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserResolverServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private UserRepository userRepository;

    private UserResolverService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserResolverService(patientRepository, doctorRepository, staffRepository, userRepository);
    }

    @Test
    void findUserById_patientFound() {
        Patient p = new Patient(); p.setId("p1");
        when(patientRepository.findById("p1")).thenReturn(Optional.of(p));
        User u = service.findUserById("p1");
        assertEquals("p1", u.getId());
    }

    @Test
    void findUserById_notFound_throws() {
        when(patientRepository.findById("x")).thenReturn(Optional.empty());
        when(doctorRepository.findById("x")).thenReturn(Optional.empty());
        when(staffRepository.findById("x")).thenReturn(Optional.empty());
        when(userRepository.findById("x")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.findUserById("x"));
    }

    @Test
    void findUserByEmail_doctorFallback() {
        when(patientRepository.findByEmail("d@e.com")).thenReturn(Optional.empty());
        Doctor d = new Doctor(); d.setId("d1"); d.setEmail("d@e.com");
        when(doctorRepository.findByEmail("d@e.com")).thenReturn(Optional.of(d));

        User u = service.findUserByEmail("d@e.com");
        assertEquals("d1", u.getId());
    }

    @Test
    void emailExists_and_emailExistsForDifferentUser_behaviour() {
        when(patientRepository.existsByEmail("a@e.com")).thenReturn(true);
        assertTrue(service.emailExists("a@e.com"));

        Patient p = new Patient(); p.setId("p1"); p.setEmail("a@e.com");
        when(userRepository.findByEmail("a@e.com")).thenReturn(Optional.of(p));
        // different user id
        assertTrue(service.emailExistsForDifferentUser("a@e.com", "other"));
        // same id => false
        assertFalse(service.emailExistsForDifferentUser("a@e.com", "p1"));
    }

    @Test
    void helper_findMethods_delegate() {
        Patient p = new Patient(); p.setId("p2");
        when(patientRepository.findById("p2")).thenReturn(Optional.of(p));
        assertTrue(service.findPatientById("p2").isPresent());
        when(patientRepository.findByEmail("e")).thenReturn(Optional.of(p));
        assertTrue(service.findPatientByEmail("e").isPresent());
    }
}

