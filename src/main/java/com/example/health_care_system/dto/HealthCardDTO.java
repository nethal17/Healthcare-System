package com.example.health_care_system.dto;

import com.example.health_care_system.model.HealthCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthCardDTO {
    private String id;
    private String cardID;
    private String qrCode;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private HealthCard.CardStatus status;
    private String userName; // User's name for display on card
    private String userEmail; // User's email
}
