package com.example.health_care_system.service;

import com.example.health_care_system.dto.LoginRequest;
import com.example.health_care_system.dto.RegisterRequest;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.User;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private QRCodeService qrCodeService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private Patient patient;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setName("Test Patient");
        registerRequest.setEmail("patient@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");
        registerRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        registerRequest.setGender("Male");
        registerRequest.setAddress("123 Test Street");
        registerRequest.setContactNumber("0771234567");

        // Setup patient
        patient = new Patient();
        patient.setId("123");
        patient.setName("Test Patient");
        patient.setEmail("patient@test.com");
        patient.setPassword("encodedPassword");
        patient.setRole(UserRole.PATIENT);
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender("Male");
        patient.setAddress("123 Test Street");
        patient.setContactNumber("0771234567");
        patient.setActive(true);
        patient.setCreatedAt(LocalDateTime.now());
        patient.setUpdatedAt(LocalDateTime.now());

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("patient@test.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegisterPatient_Success() {
        // Given
        when(patientRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(qrCodeService.generateQRCode(any())).thenReturn("qrCodeData");
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        // When
        UserDTO result = userService.registerPatient(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals("Test Patient", result.getName());
        assertEquals("patient@test.com", result.getEmail());
        assertEquals(UserRole.PATIENT, result.getRole());
        verify(patientRepository, times(2)).save(any(Patient.class));
    }

    @Test
    void testRegisterPatient_EmailAlreadyExists() {
        // Given
        when(patientRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerPatient(registerRequest);
        });
        assertEquals("Email already registered", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testRegisterPatient_PasswordMismatch() {
        // Given
        registerRequest.setConfirmPassword("differentPassword");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerPatient(registerRequest);
        });
        assertEquals("Passwords do not match", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Given
        when(patientRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(patient));
        when(passwordEncoder.matches(loginRequest.getPassword(), patient.getPassword())).thenReturn(true);

        // When
        UserDTO result = userService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("Test Patient", result.getName());
        assertEquals("patient@test.com", result.getEmail());
        assertEquals(UserRole.PATIENT, result.getRole());
    }

    @Test
    void testLogin_InvalidEmail() {
        // Given
        when(patientRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());
        when(doctorRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Given
        when(patientRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(patient));
        when(passwordEncoder.matches(loginRequest.getPassword(), patient.getPassword())).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLogin_InactiveAccount() {
        // Given
        patient.setActive(false);
        when(patientRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(patient));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
        assertEquals("Account is inactive", exception.getMessage());
    }

    @Test
    void testGetUserById_Success() {
        // Given
        when(patientRepository.findById("123")).thenReturn(Optional.of(patient));

        // When
        UserDTO result = userService.getUserById("123");

        // Then
        assertNotNull(result);
        assertEquals("Test Patient", result.getName());
        assertEquals("patient@test.com", result.getEmail());
    }

    @Test
    void testGetUserById_NotFound() {
        // Given
        when(patientRepository.findById("123")).thenReturn(Optional.empty());
        when(doctorRepository.findById("123")).thenReturn(Optional.empty());
        when(userRepository.findById("123")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById("123");
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetUserByEmail_Success() {
        // Given
        when(patientRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));

        // When
        UserDTO result = userService.getUserByEmail("patient@test.com");

        // Then
        assertNotNull(result);
        assertEquals("Test Patient", result.getName());
        assertEquals("patient@test.com", result.getEmail());
    }

    @Test
    void testGetUserByEmail_NotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserByEmail("nonexistent@test.com");
        });
        assertEquals("User not found", exception.getMessage());
    }
}
