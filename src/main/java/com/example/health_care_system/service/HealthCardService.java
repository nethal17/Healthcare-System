package com.example.health_care_system.service;

import com.example.health_care_system.dto.HealthCardDTO;
import com.example.health_care_system.model.HealthCard;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.repository.HealthCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCardService {
    
    private final HealthCardRepository healthCardRepository;
    private final QRCodeService qrCodeService;
    
    /**
     * Create a new health card for a patient
     */
    public HealthCard createHealthCard(Patient patient) {
        // Check if health card already exists
        Optional<HealthCard> existingCard = healthCardRepository.findByPatientId(patient.getId());
        if (existingCard.isPresent()) {
            log.info("Health card already exists for patient: {}", patient.getId());
            return existingCard.get();
        }
        
        // Generate QR code for the patient
        String qrCode = qrCodeService.generateQRCode(patient.getId());
        
        // Create new health card
        HealthCard healthCard = new HealthCard();
        healthCard.setPatientId(patient.getId());
        healthCard.setPatientName(patient.getName());
        healthCard.setBloodGroup(patient.getBloodGroup());
        healthCard.setQrCode(qrCode);
        healthCard.setStatus("ACTIVE");
        healthCard.setCreateDate(LocalDate.now());
        healthCard.setExpireDate(LocalDate.now().plusYears(1)); // Valid for 1 year
        healthCard.setCreatedAt(LocalDateTime.now());
        healthCard.setUpdatedAt(LocalDateTime.now());
        
        HealthCard savedCard = healthCardRepository.save(healthCard);
        log.info("Health card created successfully for patient: {}", patient.getId());
        
        return savedCard;
    }
    
    /**
     * Get health card by patient ID
     */
    public Optional<HealthCard> getHealthCardByPatientId(String patientId) {
        return healthCardRepository.findByPatientId(patientId);
    }
    
    /**
     * Get health card by ID
     */
    public Optional<HealthCard> getHealthCardById(String healthCardId) {
        return healthCardRepository.findById(healthCardId);
    }
    
    /**
     * Update health card status
     */
    public HealthCard updateHealthCardStatus(String healthCardId, String status) {
        HealthCard healthCard = healthCardRepository.findById(healthCardId)
                .orElseThrow(() -> new RuntimeException("Health card not found"));
        
        healthCard.setStatus(status);
        healthCard.setUpdatedAt(LocalDateTime.now());
        
        return healthCardRepository.save(healthCard);
    }
    
    /**
     * Update health card
     */
    public HealthCard updateHealthCard(HealthCard healthCard) {
        return healthCardRepository.save(healthCard);
    }
    
    /**
     * Renew health card (extend expiry date)
     */
    public HealthCard renewHealthCard(String healthCardId) {
        HealthCard healthCard = healthCardRepository.findById(healthCardId)
                .orElseThrow(() -> new RuntimeException("Health card not found"));
        
        healthCard.setExpireDate(LocalDate.now().plusYears(1));
        healthCard.setStatus("ACTIVE");
        healthCard.setUpdatedAt(LocalDateTime.now());
        
        return healthCardRepository.save(healthCard);
    }
    
    /**
     * Check if health card is expired
     */
    public boolean isHealthCardExpired(HealthCard healthCard) {
        return LocalDate.now().isAfter(healthCard.getExpireDate());
    }
    
    /**
     * Convert HealthCard to DTO
     */
    public HealthCardDTO convertToDTO(HealthCard healthCard) {
        HealthCardDTO dto = new HealthCardDTO();
        dto.setId(healthCard.getId());
        dto.setPatientId(healthCard.getPatientId());
        dto.setPatientName(healthCard.getPatientName());
        dto.setBloodGroup(healthCard.getBloodGroup());
        dto.setQrCode(healthCard.getQrCode());
        dto.setStatus(healthCard.getStatus());
        dto.setCreateDate(healthCard.getCreateDate());
        dto.setExpireDate(healthCard.getExpireDate());
        return dto;
    }
    
    /**
     * Generate a visual health card image with all details
     */
    public byte[] generateHealthCardImage(HealthCard healthCard) throws IOException {
        // Card dimensions (Credit card ratio - 1.586:1)
        int cardWidth = 1000;
        int cardHeight = 630;
        
        // Create buffered image with higher quality
        BufferedImage cardImage = new BufferedImage(cardWidth, cardHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = cardImage.createGraphics();
        
        // Enable anti-aliasing for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Draw simple white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, cardWidth, cardHeight);
        
        // Draw subtle light blue border
        g2d.setColor(new Color(191, 219, 254)); // Light blue 200
        g2d.setStroke(new BasicStroke(8));
        g2d.drawRoundRect(4, 4, cardWidth - 8, cardHeight - 8, 30, 30);
        
        // ==== HEADER SECTION ====
        // Draw light blue header background
        g2d.setColor(new Color(219, 234, 254)); // Light blue 100
        g2d.fillRoundRect(30, 30, cardWidth - 60, 100, 20, 20);
        
        // Draw header text
        g2d.setColor(new Color(30, 64, 175)); // Blue 800
        g2d.setFont(new Font("SansSerif", Font.BOLD, 40));
        g2d.drawString("HEALTH CARD", 50, 90);
        
        // Draw subtitle
        g2d.setColor(new Color(96, 165, 250)); // Blue 400
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2d.drawString("Official Medical Identification", 50, 115);
        
        // Draw status badge
        int badgeX = cardWidth - 160;
        int badgeY = 50;
        if (isHealthCardExpired(healthCard)) {
            g2d.setColor(new Color(254, 202, 202)); // Red 200
        } else {
            g2d.setColor(new Color(187, 247, 208)); // Green 200
        }
        g2d.fillRoundRect(badgeX, badgeY, 110, 45, 23, 23);
        
        if (isHealthCardExpired(healthCard)) {
            g2d.setColor(new Color(185, 28, 28)); // Red 700
        } else {
            g2d.setColor(new Color(21, 128, 61)); // Green 700
        }
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        String statusText = isHealthCardExpired(healthCard) ? "EXPIRED" : "ACTIVE";
        FontMetrics statusFm = g2d.getFontMetrics();
        int statusWidth = statusFm.stringWidth(statusText);
        g2d.drawString(statusText, badgeX + (110 - statusWidth) / 2, badgeY + 28);
        
        // ==== MAIN CONTENT SECTION ====
        int contentY = 160;
        
        // Patient Name Section
        g2d.setColor(new Color(147, 197, 253)); // Blue 300
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("PATIENT NAME", 50, contentY + 20);
        
        g2d.setColor(new Color(17, 24, 39)); // Gray 900
        g2d.setFont(new Font("SansSerif", Font.BOLD, 30));
        g2d.drawString(healthCard.getPatientName().toUpperCase(), 50, contentY + 55);
        
        // Draw divider line
        g2d.setColor(new Color(219, 234, 254)); // Light blue 100
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(50, contentY + 75, cardWidth - 300, contentY + 75);
        
        // Card ID Section - Only show last 6 digits
        g2d.setColor(new Color(147, 197, 253)); // Blue 300
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("CARD NUMBER", 50, contentY + 110);
        
        g2d.setColor(new Color(55, 65, 81)); // Gray 700
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
        String fullCardId = healthCard.getId();
        // Show only last 12 digits
        String lastTwelveDigits = fullCardId.substring(Math.max(0, fullCardId.length() - 12));
        g2d.drawString(lastTwelveDigits.toUpperCase(), 50, contentY + 140);
        
        // Blood Group Section
        g2d.setColor(new Color(147, 197, 253)); // Blue 300
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("BLOOD GROUP", 400, contentY + 110);
        
        g2d.setColor(new Color(220, 38, 38)); // Red 600
        g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
        String bloodGroup = healthCard.getBloodGroup() != null && !healthCard.getBloodGroup().isEmpty() 
                            ? healthCard.getBloodGroup() : "N/A";
        g2d.drawString(bloodGroup, 400, contentY + 145);
        
        // Date Information Section
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        
        // Issue Date
        g2d.setColor(new Color(147, 197, 253)); // Blue 300
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("ISSUED", 50, contentY + 185);
        
        g2d.setColor(new Color(55, 65, 81)); // Gray 700
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2d.drawString(healthCard.getCreateDate().format(formatter), 50, contentY + 210);
        
        // Expiry Date
        g2d.setColor(new Color(147, 197, 253)); // Blue 300
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("EXPIRES", 250, contentY + 185);
        
        g2d.setColor(new Color(55, 65, 81)); // Gray 700
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2d.drawString(healthCard.getExpireDate().format(formatter), 250, contentY + 210);
        
        // Validity indicator
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        if (isHealthCardExpired(healthCard)) {
            g2d.setColor(new Color(220, 38, 38)); // Red 600
            g2d.drawString("⚠ Card Expired - Please Renew", 50, contentY + 255);
        } else {
            g2d.setColor(new Color(22, 163, 74)); // Green 600
            g2d.drawString("✓ Valid for Medical Services", 50, contentY + 255);
        }
        
        // ==== QR CODE SECTION ====
        if (healthCard.getQrCode() != null && !healthCard.getQrCode().isEmpty()) {
            try {
                // Extract base64 data
                String base64Data = healthCard.getQrCode();
                if (base64Data.startsWith("data:image/png;base64,")) {
                    base64Data = base64Data.substring("data:image/png;base64,".length());
                }
                
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                
                int qrSize = 240;
                int qrX = cardWidth - qrSize - 60;
                int qrY = contentY + 10;
                
                // Draw light background for QR code
                g2d.setColor(new Color(239, 246, 255)); // Blue 50
                g2d.fillRoundRect(qrX - 20, qrY - 20, qrSize + 40, qrSize + 75, 20, 20);
                
                // Draw simple border
                g2d.setColor(new Color(191, 219, 254)); // Light blue 200
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(qrX - 20, qrY - 20, qrSize + 40, qrSize + 75, 20, 20);
                
                // Draw QR code
                g2d.drawImage(qrImage, qrX, qrY, qrSize, qrSize, null);
                
                // Draw "SCAN HERE" label
                g2d.setColor(new Color(59, 130, 246)); // Blue 500
                g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
                String scanText = "SCAN HERE";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(scanText);
                g2d.drawString(scanText, qrX + (qrSize - textWidth) / 2, qrY + qrSize + 30);
                
            } catch (Exception e) {
                log.error("Error drawing QR code on health card", e);
            }
        }
        
        // ==== FOOTER SECTION ====
        // Draw light footer background
        g2d.setColor(new Color(239, 246, 255)); // Blue 50
        g2d.fillRoundRect(30, cardHeight - 80, cardWidth - 60, 50, 20, 20);
        
        // Draw footer text
        g2d.setColor(new Color(55, 65, 81)); // Gray 700
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("Healthcare System", 50, cardHeight - 47);
        
        g2d.setColor(new Color(107, 114, 128)); // Gray 500
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.drawString("Keep this card safe and present at every visit  •  Emergency: +1-800-HEALTH", 230, cardHeight - 47);
        
        g2d.dispose();
        
        // Convert to byte array with high quality
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(cardImage, "PNG", baos);
        return baos.toByteArray();
    }
}
