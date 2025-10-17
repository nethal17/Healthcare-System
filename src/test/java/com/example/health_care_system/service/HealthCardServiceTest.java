package com.example.health_care_system.service;

import com.example.health_care_system.model.HealthCard;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.repository.HealthCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthCardServiceTest {

    @Mock
    private HealthCardRepository healthCardRepository;

    @Mock
    private QRCodeService qrCodeService;

    private HealthCardService healthCardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        healthCardService = new HealthCardService(healthCardRepository, qrCodeService);
    }

    @Test
    void createHealthCard_existing_returnsExisting() {
        Patient patient = new Patient();
        patient.setId("p1");
        HealthCard existing = new HealthCard();
        existing.setId("hc1");
        when(healthCardRepository.findByPatientId("p1")).thenReturn(Optional.of(existing));

        HealthCard result = healthCardService.createHealthCard(patient);
        assertEquals(existing, result);
        verify(healthCardRepository, never()).save(any());
    }

    @Test
    void createHealthCard_createsNew() {
        Patient patient = new Patient();
        patient.setId("p2");
        patient.setName("John Doe");
        when(healthCardRepository.findByPatientId("p2")).thenReturn(Optional.empty());
        when(qrCodeService.generateQRCode("p2")).thenReturn("data:image/png;base64,AAA");

        HealthCard saved = new HealthCard();
        saved.setId("hc2");
        when(healthCardRepository.save(any())).thenReturn(saved);

        HealthCard result = healthCardService.createHealthCard(patient);
        assertNotNull(result);
        assertEquals("hc2", result.getId());
        verify(healthCardRepository).save(any());
    }

    @Test
    void updateHealthCardStatus_and_renew_and_isExpired_and_convertToDTO() {
        HealthCard card = new HealthCard();
        card.setId("hc3");
        card.setExpireDate(LocalDate.now().minusDays(1));
        when(healthCardRepository.findById("hc3")).thenReturn(Optional.of(card));
        when(healthCardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        HealthCard updated = healthCardService.updateHealthCardStatus("hc3", "INACTIVE");
        assertEquals("INACTIVE", updated.getStatus());

        // The card should be expired before renewal
        assertTrue(healthCardService.isHealthCardExpired(card));

        HealthCard renewed = healthCardService.renewHealthCard("hc3");
        assertEquals("ACTIVE", renewed.getStatus());
        assertTrue(renewed.getExpireDate().isAfter(LocalDate.now()));

        // After renewal the card should no longer be expired
        assertFalse(healthCardService.isHealthCardExpired(renewed));

        var dto = healthCardService.convertToDTO(renewed);
        assertEquals(renewed.getId(), dto.getId());
    }

    @Test
    void generateHealthCardImage_createsImage() throws IOException {
        HealthCard card = new HealthCard();
        card.setId("hc4");
        card.setPatientName("Alice");
        card.setCreateDate(LocalDate.now());
        card.setExpireDate(LocalDate.now().plusYears(1));
        // Provide a small valid QR image base64 using QRCodeService
        when(qrCodeService.generateQRCode(any())).thenReturn("data:image/png;base64,AAA");
        // But to test drawing we set the qrCode as empty to skip image drawing branch
        card.setQrCode(null);

        byte[] bytes = healthCardService.generateHealthCardImage(card);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }
}
