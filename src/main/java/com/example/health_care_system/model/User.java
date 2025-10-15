package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    private String name;
    
    @Indexed(unique = true)
    private String email;
    
    private String password;
    
    private UserRole role;
    
    private LocalDate dateOfBirth;
    
    private String gender;
    
    private String address;
    
    private String contactNumber;
    
    private boolean active = true;
    
    private String qrCode; // Base64 encoded QR code image
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
