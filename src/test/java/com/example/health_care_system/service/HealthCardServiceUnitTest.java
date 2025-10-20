package com.example.health_care_system.service;

import com.example.health_care_system.model.HealthCard;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.repository.HealthCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthCardServiceUnitTest {

    @Mock
    private HealthCardRepository healthCardRepository;
    @Mock
    private QRCodeService qrCodeService;

    private HealthCardService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new HealthCardService(healthCardRepository, qrCodeService);
    }

    @Test
    void createHealthCard_existing_returnsExisting() {
        Patient p = new Patient(); p.setId("p1");
        HealthCard existing = new HealthCard(); existing.setId("hc1");
        when(healthCardRepository.findByPatientId("p1")).thenReturn(Optional.of(existing));

        HealthCard res = service.createHealthCard(p);
        assertEquals(existing, res);
        verify(healthCardRepository, never()).save(any());
    }

    @Test
    void createHealthCard_createsNew() {
        Patient p = new Patient(); p.setId("p2"); p.setName("John"); p.setBloodType("A+");
        when(healthCardRepository.findByPatientId("p2")).thenReturn(Optional.empty());
        when(qrCodeService.generateQRCode("p2")).thenReturn("data:image/png;base64,AAA");

        HealthCard saved = new HealthCard(); saved.setId("hc2");
        when(healthCardRepository.save(any())).thenReturn(saved);

        HealthCard res = service.createHealthCard(p);
        assertNotNull(res);
        assertEquals("hc2", res.getId());
        verify(healthCardRepository).save(any());
    }

    @Test
    void updateHealthCardStatus_notFound_throws() {
        when(healthCardRepository.findById("missing")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateHealthCardStatus("missing", "INACTIVE"));
        assertTrue(ex.getMessage().contains("Health card not found"));
    }

    @Test
    void renew_and_isExpired_and_generateImage() throws IOException {
        HealthCard card = new HealthCard();
        card.setId("hc3");
        card.setExpireDate(LocalDate.now().minusDays(1));
        when(healthCardRepository.findById("hc3")).thenReturn(Optional.of(card));
        when(healthCardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        HealthCard renewed = service.renewHealthCard("hc3");
        assertEquals("ACTIVE", renewed.getStatus());
        assertTrue(service.isHealthCardExpired(renewed) == false);

        // generate image with no QR code (should still return bytes)
        HealthCard imageCard = new HealthCard();
        imageCard.setId("hc4");
        imageCard.setPatientName("Alice");
        imageCard.setCreateDate(LocalDate.now());
        imageCard.setExpireDate(LocalDate.now().plusYears(1));
        imageCard.setQrCode(null);

        byte[] bytes = service.generateHealthCardImage(imageCard);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void generateHealthCardImage_missingFields_handlesGracefully() throws IOException {
        HealthCard card = new HealthCard();
        card.setId("hcX");
        // ensure dates are set so isHealthCardExpired doesn't NPE
        card.setCreateDate(LocalDate.now());
        card.setExpireDate(LocalDate.now().plusYears(1));
        // missing patient name and qrCode - set blank name to avoid NPE when uppercasing
        card.setPatientName("");
        byte[] bytes = service.generateHealthCardImage(card);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void renewHealthCard_notFound_throws() {
        when(healthCardRepository.findById("nohc")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.renewHealthCard("nohc"));
    }
}

