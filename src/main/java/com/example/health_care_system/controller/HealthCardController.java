package com.example.health_care_system.controller;

import com.example.health_care_system.dto.HealthCardDTO;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.HealthCard;
import com.example.health_care_system.service.HealthCardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Controller
@RequestMapping("/healthcard")
@RequiredArgsConstructor
public class HealthCardController {
    
    private final HealthCardService healthCardService;
    
    /**
     * Download health card as an image with user details
     */
    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<byte[]> downloadHealthCard(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        if (user.getHealthCard() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        try {
            byte[] imageBytes = generateHealthCardImage(user);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "health-card-" + user.getHealthCard().getCardID() + ".png");
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Scan health card by card ID
     */
    @GetMapping("/scan/{cardID}")
    @ResponseBody
    public ResponseEntity<HealthCard.ScanResult> scanHealthCard(@PathVariable String cardID) {
        try {
            HealthCard.ScanResult result = healthCardService.scanHealthCard(cardID);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Generates a health card image with user details and QR code
     */
    private byte[] generateHealthCardImage(UserDTO user) throws IOException {
        HealthCardDTO healthCard = user.getHealthCard();
        
        // Create image dimensions
        int width = 800;
        int height = 500;
        
        BufferedImage cardImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = cardImage.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Background gradient
        GradientPaint gradient = new GradientPaint(0, 0, new Color(102, 126, 234), width, height, new Color(118, 75, 162));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // White card area
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(20, 20, width - 40, height - 40, 20, 20);
        
        // Header
        g2d.setColor(new Color(102, 126, 234));
        g2d.fillRoundRect(20, 20, width - 40, 80, 20, 20);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        g2d.drawString("HEALTH CARD", 50, 70);
        
        // User information
        g2d.setColor(new Color(51, 51, 51));
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString(user.getName(), 50, 150);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("Card ID: " + healthCard.getCardID(), 50, 190);
        g2d.drawString("Email: " + user.getEmail(), 50, 220);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        g2d.drawString("Issue Date: " + healthCard.getIssueDate().format(formatter), 50, 250);
        g2d.drawString("Expiry Date: " + healthCard.getExpiryDate().format(formatter), 50, 280);
        
        // Status
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        if (healthCard.getStatus() == HealthCard.CardStatus.ACTIVE) {
            g2d.setColor(new Color(34, 197, 94));
        } else {
            g2d.setColor(new Color(239, 68, 68));
        }
        g2d.drawString("Status: " + healthCard.getStatus(), 50, 310);
        
        // QR Code
        if (healthCard.getQrCode() != null) {
            try {
                // Extract base64 data
                String base64Data = healthCard.getQrCode().replace("data:image/png;base64,", "");
                byte[] qrBytes = Base64.getDecoder().decode(base64Data);
                
                // Convert to BufferedImage
                java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(qrBytes);
                BufferedImage qrImage = ImageIO.read(bis);
                
                // Draw QR code on the right side
                g2d.drawImage(qrImage, width - 250, 120, 200, 200, null);
            } catch (Exception e) {
                // QR code rendering failed, skip it
            }
        }
        
        g2d.dispose();
        
        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(cardImage, "PNG", baos);
        return baos.toByteArray();
    }
}
