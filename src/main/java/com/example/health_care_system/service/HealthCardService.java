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
        // Card dimensions
        int cardWidth = 800;
        int cardHeight = 500;
        
        // Create buffered image
        BufferedImage cardImage = new BufferedImage(cardWidth, cardHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = cardImage.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(102, 126, 234), // #667eea
            cardWidth, cardHeight, new Color(118, 75, 162) // #764ba2
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, cardWidth, cardHeight, 30, 30);
        
        // Draw white rounded rectangle for content
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(20, 20, cardWidth - 40, cardHeight - 40, 20, 20);
        
        // Draw header with gradient
        g2d.setPaint(gradient);
        g2d.fillRoundRect(30, 30, cardWidth - 60, 80, 15, 15);
        
        // Draw header text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.drawString("HEALTH CARD", 50, 80);
        
        // Draw card info
        g2d.setColor(new Color(51, 51, 51));
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("Patient Name:", 50, 160);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 22));
        g2d.drawString(healthCard.getPatientName(), 50, 195);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Card ID: " + healthCard.getId().substring(0, Math.min(12, healthCard.getId().length())), 50, 240);
        
        g2d.drawString("Status: " + healthCard.getStatus(), 50, 275);
        
        // Format dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        g2d.drawString("Issue Date: " + healthCard.getCreateDate().format(formatter), 50, 310);
        g2d.drawString("Expiry Date: " + healthCard.getExpireDate().format(formatter), 50, 345);
        
        // Check if expired
        if (isHealthCardExpired(healthCard)) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("EXPIRED", 50, 380);
        } else {
            g2d.setColor(new Color(34, 197, 94));
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("VALID", 50, 380);
        }
        
        // Draw QR code on the right side
        if (healthCard.getQrCode() != null && !healthCard.getQrCode().isEmpty()) {
            try {
                // Extract base64 data
                String base64Data = healthCard.getQrCode();
                if (base64Data.startsWith("data:image/png;base64,")) {
                    base64Data = base64Data.substring("data:image/png;base64,".length());
                }
                
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                
                // Draw QR code
                int qrSize = 200;
                int qrX = cardWidth - qrSize - 80;
                int qrY = 140;
                
                // Draw white background for QR code
                g2d.setColor(Color.WHITE);
                g2d.fillRect(qrX - 10, qrY - 10, qrSize + 20, qrSize + 20);
                
                // Draw border
                g2d.setColor(new Color(102, 126, 234));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(qrX - 10, qrY - 10, qrSize + 20, qrSize + 20);
                
                // Draw QR code image
                g2d.drawImage(qrImage, qrX, qrY, qrSize, qrSize, null);
                
                // Draw "Scan Me" text
                g2d.setColor(new Color(51, 51, 51));
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                String scanText = "Scan for Details";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(scanText);
                g2d.drawString(scanText, qrX + (qrSize - textWidth) / 2, qrY + qrSize + 30);
                
            } catch (Exception e) {
                log.error("Error drawing QR code on health card", e);
            }
        }
        
        // Draw footer
        g2d.setColor(new Color(156, 163, 175));
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        g2d.drawString("Healthcare System - Keep this card safe and present it at every visit", 50, cardHeight - 40);
        
        g2d.dispose();
        
        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(cardImage, "PNG", baos);
        return baos.toByteArray();
    }
}
