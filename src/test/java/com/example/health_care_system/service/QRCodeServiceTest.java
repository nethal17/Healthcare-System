package com.example.health_care_system.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QRCodeServiceTest {
    private final QRCodeService qrCodeService = new QRCodeService();

    @Test
    void generateQRCode_shouldReturnBase64PngPrefixed() {
        String userId = "user123";
        String result = qrCodeService.generateQRCode(userId);
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
        assertTrue(result.length() > "data:image/png;base64,".length());
    }

    @Test
    void extractUserIdFromQRContent_valid() {
        String content = "HEALTHCARE_USER:abc-123";
        String userId = qrCodeService.extractUserIdFromQRContent(content);
        assertEquals("abc-123", userId);
    }

    @Test
    void extractUserIdFromQRContent_invalid() {
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.extractUserIdFromQRContent("INVALID"));
    }
}

