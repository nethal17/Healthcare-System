package com.example.health_care_system.service;

import com.example.health_care_system.dto.LoginRequest;
import com.example.health_care_system.dto.RegisterRequest;
import com.example.health_care_system.dto.UpdateProfileRequest;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Doctor;
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
    private final QRCodeService qrCodeService;
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
        
        // Generate QR code for patient using MongoDB ObjectId
        String qrCode = qrCodeService.generateQRCode(savedPatient.getId());
        savedPatient.setQrCode(qrCode);
        savedPatient = patientRepository.save(savedPatient);
        
        // Create health card for the patient
        healthCardService.createHealthCard(savedPatient);
        
        return convertToDTO(savedPatient);
    }
    
    public UserDTO login(LoginRequest request) {
        // Try to find as Patient first
        Optional<Patient> patientOptional = patientRepository.findByEmail(request.getEmail());
        if (patientOptional.isPresent()) {
            Patient patient = patientOptional.get();
            
            if (!patient.isActive()) {
                throw new RuntimeException("Account is inactive");
            }
            
            if (!passwordEncoder.matches(request.getPassword(), patient.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }
            
            // Generate QR code if not exists
            if (patient.getQrCode() == null || patient.getQrCode().isEmpty()) {
                String qrCode = qrCodeService.generateQRCode(patient.getId());
                patient.setQrCode(qrCode);
                patientRepository.save(patient);
            }
            
            // Create health card if not exists
            if (!healthCardService.getHealthCardByPatientId(patient.getId()).isPresent()) {
                healthCardService.createHealthCard(patient);
            }
            
            return convertToDTO(patient);
        }
        
        // Try to find as Doctor
        Optional<Doctor> doctorOptional = doctorRepository.findByEmail(request.getEmail());
        if (doctorOptional.isPresent()) {
            Doctor doctor = doctorOptional.get();
            
            if (!passwordEncoder.matches(request.getPassword(), doctor.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }
            
            return convertToDTO(doctor);
        }
        
        // Try to find as regular User (ADMIN, STAFF)
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }
            
            return convertToDTO(user);
        }
        
        throw new RuntimeException("Invalid email or password");
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
        
        // Handle Patient-specific fields
        if (user instanceof Patient) {
            Patient patient = (Patient) user;
            dto.setDateOfBirth(patient.getDateOfBirth());
            dto.setAddress(patient.getAddress());
            dto.setQrCode(patient.getQrCode());
            
            // Include health card information
            healthCardService.getHealthCardByPatientId(patient.getId()).ifPresent(healthCard -> {
                dto.setHealthCard(healthCardService.convertToDTO(healthCard));
            });
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
        // Try to find as Patient first
        Optional<Patient> patientOptional = patientRepository.findById(userId);
        if (patientOptional.isPresent()) {
            Patient patient = patientOptional.get();
            
            // Check if email is being changed and if it's already taken by another user
            if (!patient.getEmail().equals(request.getEmail())) {
                if (patientRepository.existsByEmail(request.getEmail())) {
                    throw new RuntimeException("Email already registered");
                }
            }
            
            // Update patient details
            patient.setName(request.getName());
            patient.setEmail(request.getEmail());
            patient.setContactNumber(request.getContactNumber());
            patient.setDateOfBirth(request.getDateOfBirth());
            patient.setGender(request.getGender());
            patient.setAddress(request.getAddress());
            patient.setUpdatedAt(LocalDateTime.now());
            
            Patient updatedPatient = patientRepository.save(patient);
            
            // Update health card if exists
            healthCardService.getHealthCardByPatientId(patient.getId()).ifPresent(healthCard -> {
                healthCard.setPatientName(request.getName());
                healthCard.setUpdatedAt(LocalDateTime.now());
                healthCardService.updateHealthCard(healthCard);
            });
            
            return convertToDTO(updatedPatient);
        }
        
        // Try to find as Doctor
        Optional<Doctor> doctorOptional = doctorRepository.findById(userId);
        if (doctorOptional.isPresent()) {
            Doctor doctor = doctorOptional.get();
            
            // Check if email is being changed and if it's already taken
            if (!doctor.getEmail().equals(request.getEmail())) {
                if (doctorRepository.existsByEmail(request.getEmail())) {
                    throw new RuntimeException("Email already registered");
                }
            }
            
            // Update doctor details
            doctor.setName(request.getName());
            doctor.setEmail(request.getEmail());
            doctor.setContactNumber(request.getContactNumber());
            doctor.setGender(request.getGender());
            doctor.setUpdatedAt(LocalDateTime.now());
            
            Doctor updatedDoctor = doctorRepository.save(doctor);
            return convertToDTO(updatedDoctor);
        }
        
        // Try to find as regular User (ADMIN, STAFF)
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Check if email is being changed and if it's already taken
            if (!user.getEmail().equals(request.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new RuntimeException("Email already registered");
                }
            }
            
            // Update user details
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setContactNumber(request.getContactNumber());
            user.setGender(request.getGender());
            user.setUpdatedAt(LocalDateTime.now());
            
            User updatedUser = userRepository.save(user);
            return convertToDTO(updatedUser);
        }
        
        throw new RuntimeException("User not found");
    }
    
    public void changePassword(String userId, String currentPassword, String newPassword) {
        // Try to find as Patient first
        Optional<Patient> patientOptional = patientRepository.findById(userId);
        if (patientOptional.isPresent()) {
            Patient patient = patientOptional.get();
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, patient.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            
            // Update password
            patient.setPassword(passwordEncoder.encode(newPassword));
            patient.setUpdatedAt(LocalDateTime.now());
            patientRepository.save(patient);
            return;
        }
        
        // Try to find as Doctor
        Optional<Doctor> doctorOptional = doctorRepository.findById(userId);
        if (doctorOptional.isPresent()) {
            Doctor doctor = doctorOptional.get();
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, doctor.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            
            // Update password
            doctor.setPassword(passwordEncoder.encode(newPassword));
            doctor.setUpdatedAt(LocalDateTime.now());
            doctorRepository.save(doctor);
            return;
        }
        
        // Try to find as regular User (ADMIN, STAFF)
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            
            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return;
        }
        
        throw new RuntimeException("User not found");
    }
}
