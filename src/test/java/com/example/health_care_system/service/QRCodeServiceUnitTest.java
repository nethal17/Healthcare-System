package com.example.health_care_system.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeServiceUnitTest {

    private final QRCodeService qrCodeService = new QRCodeService();

    @Test
    void generateQRCode_returnsBase64PngPrefixed() {
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
    void extractUserIdFromQRContent_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.extractUserIdFromQRContent("INVALID"));
    }

    @Test
    void generateQRCode_nullUserId_returnsPrefixedBase64() {
        // Service currently accepts null and encodes the string; ensure format is preserved
        String result = qrCodeService.generateQRCode(null);
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
    }

    @Test
    void extractUserIdFromQRContent_empty_throws() {
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.extractUserIdFromQRContent(""));
    }
}

