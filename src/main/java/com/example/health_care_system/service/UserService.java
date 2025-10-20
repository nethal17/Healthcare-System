package com.example.health_care_system.service;

import com.example.health_care_system.dto.RegisterRequest;
import com.example.health_care_system.dto.UpdateProfileRequest;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.exception.DuplicateResourceException;
import com.example.health_care_system.exception.ValidationException;
import com.example.health_care_system.factory.UserFactory;
import com.example.health_care_system.mapper.UserMapper;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Staff;
import com.example.health_care_system.model.User;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.repository.StaffRepository;
import com.example.health_care_system.repository.UserRepository;
import com.example.health_care_system.service.validation.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final StaffRepository staffRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final QRCodeService qrCodeService;
    private final HealthCardService healthCardService;
    private final UserResolverService userResolverService;
    private final UserMapper userMapper;
    private final UserFactory userFactory;
    private final ValidationService validationService;
    
    @Transactional
    public UserDTO registerPatient(RegisterRequest request) {
        log.debug("Registering new patient with email: {}", request.getEmail());
        
        // Validate registration request
        validationService.validateRegistrationRequest(request);
        
        // Check if email already exists across all user types
        if (userResolverService.emailExists(request.getEmail())) {
            log.warn("Registration attempt with existing email: {}", request.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }
        
        // Encode password
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // Create new patient using factory
        Patient patient = userFactory.createPatient(request, encodedPassword);
        Patient savedPatient = patientRepository.save(patient);
        log.info("Successfully registered patient with ID: {}", savedPatient.getId());
        
        // Generate QR code for patient
        String qrCode = qrCodeService.generateQRCode(savedPatient.getId());
        savedPatient.setQrCode(qrCode);
        savedPatient = patientRepository.save(savedPatient);
        
        // Create health card for the patient
        healthCardService.createHealthCard(savedPatient);
        
        return userMapper.toDTO(savedPatient);
    }

    public UserDTO getUserById(String id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userResolverService.findUserById(id);
        return userMapper.toDTO(user);
    }
    
    public UserDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userResolverService.findUserByEmail(email);
        return userMapper.toDTO(user);
    }
    
    public User getUserEntityById(String id) {
        log.debug("Fetching user entity by ID: {}", id);
        return userResolverService.findUserById(id);
    }
    
    @Transactional
    public UserDTO updateProfile(String userId, UpdateProfileRequest request) {
        log.debug("Updating profile for user ID: {}", userId);
        
        // Find the user
        User user = userResolverService.findUserById(userId);
        
        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(request.getEmail())) {
            if (userResolverService.emailExists(request.getEmail())) {
                log.warn("Email change attempt to existing email: {}", request.getEmail());
                throw new DuplicateResourceException("Email already registered");
            }
        }
        
        // Update common user details
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setContactNumber(request.getContactNumber());
        user.setGender(request.getGender());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Update Patient-specific fields
        if (user instanceof Patient) {
            Patient patient = (Patient) user;
            patient.setDateOfBirth(request.getDateOfBirth());
            patient.setBloodType(request.getBloodType());
            patient.setAddress(request.getAddress());
            Patient updatedPatient = patientRepository.save(patient);
            
            // Update health card if exists
            healthCardService.getHealthCardByPatientId(patient.getId()).ifPresent(healthCard -> {
                healthCard.setPatientName(request.getName());
                healthCard.setBloodType(request.getBloodType());
                healthCard.setUpdatedAt(LocalDateTime.now());
                healthCardService.updateHealthCard(healthCard);
            });
            
            log.info("Updated patient profile for ID: {}", userId);
            return userMapper.toDTO(updatedPatient);
        }
        
        // Update Doctor
        if (user instanceof Doctor) {
            Doctor updatedDoctor = doctorRepository.save((Doctor) user);
            log.info("Updated doctor profile for ID: {}", userId);
            return userMapper.toDTO(updatedDoctor);
        }
        
        // Update Staff
        if (user instanceof Staff) {
            Staff updatedStaff = staffRepository.save((Staff) user);
            log.info("Updated staff profile for ID: {}", userId);
            return userMapper.toDTO(updatedStaff);
        }
        
        // Update regular User (ADMIN)
        User updatedUser = userRepository.save(user);
        log.info("Updated user profile for ID: {}", userId);
        return userMapper.toDTO(updatedUser);
    }
    
    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        log.debug("Changing password for user ID: {}", userId);
        
        // Validate password strength (basic validation)
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }
        
        // Find the user
        User user = userResolverService.findUserById(userId);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Incorrect current password for user ID: {}", userId);
            throw new ValidationException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save based on type
        if (user instanceof Patient) {
            patientRepository.save((Patient) user);
        } else if (user instanceof Doctor) {
            doctorRepository.save((Doctor) user);
        } else if (user instanceof Staff) {
            staffRepository.save((Staff) user);
        } else {
            userRepository.save(user);
        }
        
        log.info("Successfully changed password for user ID: {}", userId);
    }
    
    // User Management Methods for Admin
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
    
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
    
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    public int getTotalUserCount() {
        // All users (Patient, Doctor, Staff, Admin) are in the same "users" collection
        // So we just count all documents in the collection
        return (int) userRepository.count();
    }
    
    public int getPatientCount() {
        // Count users with PATIENT role
        return userRepository.findByRole(UserRole.PATIENT).size();
    }
    
    public int getDoctorCount() {
        // Count users with DOCTOR role
        return userRepository.findByRole(UserRole.DOCTOR).size();
    }
    
    public int getStaffCount() {
        return userRepository.findByRole(UserRole.STAFF).size();
    }
}
