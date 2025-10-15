package com.example.health_care_system.service;

import com.example.health_care_system.dto.HealthCardDTO;
import com.example.health_care_system.model.HealthCard;
import com.example.health_care_system.model.User;
import com.example.health_care_system.repository.HealthCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCardService {
    
    private final HealthCardRepository healthCardRepository;
    private final QRCodeService qrCodeService;
    
    private static final int CARD_VALIDITY_YEARS = 5;
    
    /**
     * Creates a new health card for a user
     */
    public HealthCard createHealthCard(User user) {
        HealthCard healthCard = new HealthCard();
        
        // Generate unique card ID
        String cardID = generateUniqueCardID();
        healthCard.setCardID(cardID);
        healthCard.setUserId(user.getId());
        
        // Set dates
        LocalDate today = LocalDate.now();
        healthCard.setIssueDate(today);
        healthCard.setExpiryDate(today.plusYears(CARD_VALIDITY_YEARS));
        
        // Set status
        healthCard.setStatus(HealthCard.CardStatus.ACTIVE);
        
        // Generate QR code with health card ID
        String qrCode = qrCodeService.generateQRCode(cardID);
        healthCard.setQrCode(qrCode);
        
        // Set timestamps
        healthCard.setCreatedAt(LocalDateTime.now());
        healthCard.setUpdatedAt(LocalDateTime.now());
        
        HealthCard saved = healthCardRepository.save(healthCard);
        log.info("Health card created for user: {} with card ID: {}", user.getId(), cardID);
        
        return saved;
    }
    
    /**
     * Gets health card by user ID
     */
    public HealthCard getHealthCardByUserId(String userId) {
        return healthCardRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Health card not found for user"));
    }
    
    /**
     * Gets health card by card ID
     */
    public HealthCard getHealthCardByCardID(String cardID) {
        return healthCardRepository.findByCardID(cardID)
                .orElseThrow(() -> new RuntimeException("Health card not found"));
    }
    
    /**
     * Converts HealthCard to DTO with user information
     */
    public HealthCardDTO convertToDTO(HealthCard healthCard, User user) {
        HealthCardDTO dto = new HealthCardDTO();
        dto.setId(healthCard.getId());
        dto.setCardID(healthCard.getCardID());
        dto.setQrCode(healthCard.getQrCode());
        dto.setIssueDate(healthCard.getIssueDate());
        dto.setExpiryDate(healthCard.getExpiryDate());
        dto.setStatus(healthCard.getStatus());
        dto.setUserName(user.getName());
        dto.setUserEmail(user.getEmail());
        return dto;
    }
    
    /**
     * Generates a unique card ID in format HC-YYYY-XXXXXX
     */
    private String generateUniqueCardID() {
        String year = String.valueOf(LocalDate.now().getYear());
        String randomPart;
        String cardID;
        
        // Keep generating until we get a unique ID
        do {
            randomPart = String.format("%06d", new Random().nextInt(1000000));
            cardID = "HC-" + year + "-" + randomPart;
        } while (healthCardRepository.existsByCardID(cardID));
        
        return cardID;
    }
    
    /**
     * Validates a health card
     */
    public HealthCard.ScanResult scanHealthCard(String cardID) {
        HealthCard healthCard = getHealthCardByCardID(cardID);
        return healthCard.scanCard();
    }
}
