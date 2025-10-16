package com.example.health_care_system.dto;

import com.example.health_care_system.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String name;
    private String email;
    private UserRole role;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String contactNumber;
    private String qrCode; // Base64 encoded QR code image
    private HealthCardDTO healthCard; // Health card information for patients
}
