package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "health_cards")
public class HealthCard {
    @Id
    private String id;
    
    private String cardID; // Unique card identifier (e.g., HC-2025-XXXXX)
    
    private String userId; // Reference to User
    
    private String qrCode; // Base64 encoded QR code image
    
    private LocalDate issueDate;
    
    private LocalDate expiryDate;
    
    private CardStatus status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    /**
     * Validates if the health card is currently valid
     * @return true if card is active and not expired, false otherwise
     */
    public boolean validateCard() {
        if (this.status != CardStatus.ACTIVE) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        return !today.isBefore(this.issueDate) && !today.isAfter(this.expiryDate);
    }
    
    /**
     * Scans the health card and returns relevant information
     * @return ScanResult containing card validation status and details
     */
    public ScanResult scanCard() {
        boolean isValid = validateCard();
        String message;
        
        if (!isValid) {
            if (this.status == CardStatus.EXPIRED) {
                message = "Health card has expired";
            } else if (this.status == CardStatus.SUSPENDED) {
                message = "Health card is suspended";
            } else if (this.status == CardStatus.CANCELLED) {
                message = "Health card has been cancelled";
            } else {
                message = "Health card is invalid";
            }
        } else {
            message = "Health card is valid";
        }
        
        return new ScanResult(isValid, message, this.cardID, this.expiryDate);
    }
    
    /**
     * Inner class for scan results
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScanResult {
        private boolean valid;
        private String message;
        private String cardID;
        private LocalDate expiryDate;
    }
    
    /**
     * Enum for card status
     */
    public enum CardStatus {
        ACTIVE,
        EXPIRED,
        SUSPENDED,
        CANCELLED
    }
}
