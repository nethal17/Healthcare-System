package com.example.health_care_system.service;

import com.example.health_care_system.dto.LoginRequest;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.exception.AuthenticationException;
import com.example.health_care_system.mapper.UserMapper;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UserResolverService userResolverService;
    @Mock
    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private QRCodeService qrCodeService;
    @Mock
    private HealthCardService healthCardService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationService = new AuthenticationService(userResolverService, passwordEncoder, userMapper, qrCodeService, healthCardService);
    }

    @Test
    void authenticate_success_patient_createsResources() {
        Patient patient = new Patient();
        patient.setId("p1");
        patient.setEmail("p@example.com");
        patient.setPassword("encoded");
        patient.setActive(true);
        // no qr code initially
        patient.setQrCode(null);

        when(userResolverService.findUserByEmail("p@example.com")).thenReturn(patient);
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);
        when(userResolverService.findPatientById("p1")).thenReturn(java.util.Optional.of(patient));
        when(healthCardService.getHealthCardByPatientId("p1")).thenReturn(java.util.Optional.empty());
        when(qrCodeService.generateQRCode("p1")).thenReturn("data:image/png;base64,AAA");
        UserDTO dto = new UserDTO(); dto.setId("p1");
        when(userMapper.toDTO(patient)).thenReturn(dto);

        LoginRequest req = new LoginRequest(); req.setEmail("p@example.com"); req.setPassword("raw");

        UserDTO res = authenticationService.authenticate(req);

        assertNotNull(res);
        assertEquals("p1", res.getId());
        verify(qrCodeService).generateQRCode("p1");
        verify(healthCardService).createHealthCard(patient);
    }

    @Test
    void authenticate_invalidPassword_throws() {
        User user = new User(); user.setPassword("encoded"); user.setEmail("u@e.com");
        when(userResolverService.findUserByEmail("u@e.com")).thenReturn(user);
        when(passwordEncoder.matches("bad", "encoded")).thenReturn(false);

        LoginRequest req = new LoginRequest(); req.setEmail("u@e.com"); req.setPassword("bad");

        AuthenticationException ex = assertThrows(AuthenticationException.class, () -> authenticationService.authenticate(req));
        assertTrue(ex.getMessage().contains("Invalid email or password"));
    }

    @Test
    void authenticate_inactivePatient_throws() {
        Patient patient = new Patient();
        patient.setId("p2");
        patient.setEmail("p2@example.com");
        patient.setPassword("encoded");
        patient.setActive(false);

        when(userResolverService.findUserByEmail("p2@example.com")).thenReturn(patient);
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        LoginRequest req = new LoginRequest(); req.setEmail("p2@example.com"); req.setPassword("raw");

        AuthenticationException ex = assertThrows(AuthenticationException.class, () -> authenticationService.authenticate(req));
        assertTrue(ex.getMessage().contains("Account is inactive"));
    }

    @Test
    void verifyAndEncodePassword_delegateToEncoder() {
        when(passwordEncoder.matches("r","e")).thenReturn(true);
        when(passwordEncoder.encode("r")).thenReturn("encodedR");

        assertTrue(authenticationService.verifyPassword("r","e"));
        assertEquals("encodedR", authenticationService.encodePassword("r"));
    }

    @Test
    void authenticate_nullRequest_throws() {
        assertThrows(NullPointerException.class, () -> authenticationService.authenticate(null));
    }

    @Test
    void authenticate_userNotFound_throws() {
        when(userResolverService.findUserByEmail("no@one.com")).thenReturn(null);
        LoginRequest req = new LoginRequest(); req.setEmail("no@one.com"); req.setPassword("x");
        // current implementation will result in NPE when user is null; assert NPE
        assertThrows(NullPointerException.class, () -> authenticationService.authenticate(req));
    }
}

