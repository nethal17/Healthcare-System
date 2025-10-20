package com.example.health_care_system.service;

import com.example.health_care_system.dto.LoginRequest;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.exception.AuthenticationException;
import com.example.health_care_system.mapper.UserMapper;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for authentication operations.
 * Follows Single Responsibility Principle - handles only authentication logic.
 * Extracted from UserService to improve cohesion and reduce coupling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserResolverService userResolverService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final QRCodeService qrCodeService;
    private final HealthCardService healthCardService;
    
    /**
     * Authenticate user with email and password
     * 
     * @param request Login credentials
     * @return UserDTO if authentication successful
     * @throws AuthenticationException if authentication fails
     */
    @Transactional
    public UserDTO authenticate(LoginRequest request) {
        log.info("Authentication attempt for email: {}", request.getEmail());
        
        // Find user by email
        User user = userResolverService.findUserByEmail(request.getEmail());
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed authentication attempt for email: {}", request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }
        
        // Check if patient account is active
        if (user instanceof Patient patient && !patient.isActive()) {
            log.warn("Inactive account login attempt: {}", request.getEmail());
            throw new AuthenticationException("Account is inactive");
        }
        
        // Ensure patient has QR code and health card
        if (user instanceof Patient patient) {
            ensurePatientResources(patient);
        }
        
        log.info("Successful authentication for user: {}", user.getId());
        return userMapper.toDTO(user);
    }
    
    /**
     * Ensure patient has QR code and health card
     * Generates them if they don't exist
     */
    private void ensurePatientResources(Patient patient) {
        boolean needsUpdate = false;
        
        // Generate QR code if not exists
        if (patient.getQrCode() == null || patient.getQrCode().isEmpty()) {
            log.info("Generating QR code for patient: {}", patient.getId());
            String qrCode = qrCodeService.generateQRCode(patient.getId());
            patient.setQrCode(qrCode);
            needsUpdate = true;
        }
        
        // Save patient if updated
        if (needsUpdate) {
            userResolverService.findPatientById(patient.getId()).ifPresent(p -> {
                p.setQrCode(patient.getQrCode());
                // Note: Repository save would be handled by the calling service
            });
        }
        
        // Create health card if not exists
        if (!healthCardService.getHealthCardByPatientId(patient.getId()).isPresent()) {
            log.info("Creating health card for patient: {}", patient.getId());
            healthCardService.createHealthCard(patient);
        }
    }
    
    /**
     * Verify password matches for a user
     * 
     * @param rawPassword Plain text password
     * @param encodedPassword Encoded password from database
     * @return true if passwords match
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * Encode password for storage
     * 
     * @param rawPassword Plain text password
     * @return Encoded password
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
