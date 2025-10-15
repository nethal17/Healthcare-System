package com.example.health_care_system.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class QRCodeService {
    
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    
    /**
     * Generate QR code for user identification
     * QR code contains user ID which can be used to fetch user details
     */
    public String generateQRCode(String userId) {
        try {
            // Create QR code content with user ID
            String qrContent = "HEALTHCARE_USER:" + userId;
            
            // Set QR code parameters
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                qrContent,
                BarcodeFormat.QR_CODE,
                QR_CODE_WIDTH,
                QR_CODE_HEIGHT,
                hints
            );
            
            // Convert to buffered image
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // Convert to Base64 string
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            log.info("QR code generated successfully for user: {}", userId);
            return "data:image/png;base64," + base64Image;
            
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code for user: {}", userId, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
    
    /**
     * Extract user ID from QR code content
     */
    public String extractUserIdFromQRContent(String qrContent) {
        if (qrContent != null && qrContent.startsWith("HEALTHCARE_USER:")) {
            return qrContent.substring("HEALTHCARE_USER:".length());
        }
        throw new IllegalArgumentException("Invalid QR code format");
    }
}
