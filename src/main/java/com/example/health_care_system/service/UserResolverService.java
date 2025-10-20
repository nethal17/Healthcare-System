package com.example.health_care_system.service;

import com.example.health_care_system.exception.UserNotFoundException;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Staff;
import com.example.health_care_system.model.User;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.repository.StaffRepository;
import com.example.health_care_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service responsible for resolving user types across different repositories.
 * Follows Single Responsibility Principle - only handles user resolution logic.
 * Eliminates code duplication from UserService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserResolverService {
    
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    
    /**
     * Find user by ID across all user type repositories
     * 
     * @param id User identifier
     * @return User entity (could be Patient, Doctor, Staff, or User)
     * @throws UserNotFoundException if user is not found
     */
    public User findUserById(String id) {
        log.debug("Resolving user by ID: {}", id);
        
        return findPatientById(id)
            .map(user -> (User) user)
            .or(() -> findDoctorById(id).map(user -> (User) user))
            .or(() -> findStaffById(id).map(user -> (User) user))
            .or(() -> userRepository.findById(id))
            .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    /**
     * Find user by email across all user type repositories
     * 
     * @param email User email
     * @return User entity (could be Patient, Doctor, Staff, or User)
     * @throws UserNotFoundException if user is not found
     */
    public User findUserByEmail(String email) {
        log.debug("Resolving user by email: {}", email);
        
        return findPatientByEmail(email)
            .map(user -> (User) user)
            .or(() -> findDoctorByEmail(email).map(user -> (User) user))
            .or(() -> findStaffByEmail(email).map(user -> (User) user))
            .or(() -> userRepository.findByEmail(email))
            .orElseThrow(() -> new UserNotFoundException("User not found with email", email));
    }
    
    /**
     * Check if email already exists in any user repository
     * 
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        return patientRepository.existsByEmail(email)
            || doctorRepository.existsByEmail(email)
            || staffRepository.existsByEmail(email)
            || userRepository.findByEmail(email).isPresent();
    }
    
    /**
     * Check if email exists for a different user (used during updates)
     * 
     * @param email Email to check
     * @param currentUserId Current user's ID to exclude
     * @return true if email exists for another user
     */
    public boolean emailExistsForDifferentUser(String email, String currentUserId) {
        try {
            User existingUser = findUserByEmail(email);
            return !existingUser.getId().equals(currentUserId);
        } catch (UserNotFoundException e) {
            return false;
        }
    }
    
    // Helper methods for individual repository lookups
    
    public Optional<Patient> findPatientById(String id) {
        return patientRepository.findById(id);
    }
    
    public Optional<Patient> findPatientByEmail(String email) {
        return patientRepository.findByEmail(email);
    }
    
    public Optional<Doctor> findDoctorById(String id) {
        return doctorRepository.findById(id);
    }
    
    public Optional<Doctor> findDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }
    
    public Optional<Staff> findStaffById(String id) {
        return staffRepository.findById(id);
    }
    
    public Optional<Staff> findStaffByEmail(String email) {
        return staffRepository.findByEmail(email);
    }
}
