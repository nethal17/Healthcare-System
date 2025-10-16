package com.example.health_care_system.service;

import com.example.health_care_system.dto.LoginRequest;
import com.example.health_care_system.dto.RegisterRequest;
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
}