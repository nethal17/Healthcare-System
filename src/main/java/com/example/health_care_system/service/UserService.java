package com.example.health_care_system.service;

import com.example.health_care_system.dto.ChangePasswordRequest;
import com.example.health_care_system.dto.HealthCardDTO;
import com.example.health_care_system.dto.LoginRequest;
import com.example.health_care_system.dto.RegisterRequest;
import com.example.health_care_system.dto.UpdateProfileRequest;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.HealthCard;
import com.example.health_care_system.model.User;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final HealthCardService healthCardService;
    
    public UserDTO registerPatient(RegisterRequest request) {
        // Check if passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        
        // Check if email already exists
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Create new patient
        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setRole(UserRole.PATIENT);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setContactNumber(request.getContactNumber());
        patient.setActive(true);
        patient.setCreatedAt(LocalDateTime.now());
        patient.setUpdatedAt(LocalDateTime.now());
        
        Patient savedPatient = patientRepository.save(patient);
        
        // Generate HealthCard only for PATIENT users
        if (savedPatient.getRole() == UserRole.PATIENT) {
            HealthCard healthCard = healthCardService.createHealthCard(savedPatient);
            savedPatient.setHealthCardId(healthCard.getId());
            savedPatient = userRepository.save(savedPatient);
        }
        
        return convertToDTO(savedPatient);
    }
    
    public UserDTO login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }
        
        User user = userOptional.get();
        
        if (!user.isActive()) {
            throw new RuntimeException("Account is inactive");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        // Generate HealthCard only for PATIENT users if not exists
        if (user.getRole() == UserRole.PATIENT && (user.getHealthCardId() == null || user.getHealthCardId().isEmpty())) {
            HealthCard healthCard = healthCardService.createHealthCard(user);
            user.setHealthCardId(healthCard.getId());
            userRepository.save(user);
        }
        
        return convertToDTO(user);
    }
    
    public UserDTO getUserById(String id) {
        // Try Patient first
        Optional<Patient> patient = patientRepository.findById(id);
        if (patient.isPresent()) {
            return convertToDTO(patient.get());
        }
        
        // Try Doctor
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isPresent()) {
            return convertToDTO(doctor.get());
        }
        
        // Try regular User
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }
    
    public UserDTO getUserByEmail(String email) {
        // Try Patient first
        Optional<Patient> patient = patientRepository.findByEmail(email);
        if (patient.isPresent()) {
            return convertToDTO(patient.get());
        }
        
        // Try Doctor
        Optional<Doctor> doctor = doctorRepository.findByEmail(email);
        if (doctor.isPresent()) {
            return convertToDTO(doctor.get());
        }
        
        // Try regular User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setGender(user.getGender());
        dto.setContactNumber(user.getContactNumber());
        
        // Add health card if user is a patient and has one
        if (user.getRole() == UserRole.PATIENT && user.getHealthCardId() != null) {
            try {
                HealthCard healthCard = healthCardService.getHealthCardByUserId(user.getId());
                HealthCardDTO healthCardDTO = healthCardService.convertToDTO(healthCard, user);
                dto.setHealthCard(healthCardDTO);
            } catch (Exception e) {
                // Health card not found, leave it null
            }
        }
        
        return dto;
    }
    
    public User getUserEntityById(String id) {
        // Try Patient first
        Optional<Patient> patient = patientRepository.findById(id);
        if (patient.isPresent()) {
            return patient.get();
        }
        
        // Try Doctor
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isPresent()) {
            return doctor.get();
        }
        
        // Try regular User
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public UserDTO updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update allowed fields
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        
        user.setContactNumber(request.getContactNumber());
        //user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        //user.setAddress(request.getAddress());
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    public void changePassword(String userId, ChangePasswordRequest request) {
        System.out.println("=== changePassword called for userId: " + userId);
        System.out.println("=== Request object: " + request);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        System.out.println("=== User found: " + user.getEmail());
        
        // Verify current password
        System.out.println("=== Verifying current password...");
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            System.out.println("=== Current password verification FAILED");
            throw new RuntimeException("Current password is incorrect");
        }
        System.out.println("=== Current password verified successfully");
        
        // Validate new password and confirmation match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            System.out.println("=== New password and confirm password do not match");
            throw new RuntimeException("New passwords do not match");
        }
        System.out.println("=== New password and confirm password match");
        
        // Validate new password length
        if (request.getNewPassword().length() < 6) {
            System.out.println("=== New password length validation FAILED");
            throw new RuntimeException("New password must be at least 6 characters long");
        }
        System.out.println("=== New password length validation passed");
        
        // Check new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            System.out.println("=== New password is same as current password");
            throw new RuntimeException("New password must be different from current password");
        }
        System.out.println("=== New password is different from current");
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        System.out.println("=== Password updated successfully");
    }
}
