package com.example.health_care_system.factory;

import com.example.health_care_system.dto.RegisterRequest;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Staff;
import com.example.health_care_system.model.User;
import com.example.health_care_system.model.UserRole;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Factory class for creating different types of User entities.
 * Follows Factory Pattern and Open/Closed Principle.
 * Centralizes user creation logic for better maintainability.
 */
@Component
public class UserFactory {
    
    /**
     * Create a Patient from registration request
     * 
     * @param request Registration data
     * @param encodedPassword Already encoded password
     * @return New Patient instance
     */
    public Patient createPatient(RegisterRequest request, String encodedPassword) {
        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setPassword(encodedPassword);
        patient.setRole(UserRole.PATIENT);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodType(request.getBloodType());
        patient.setAddress(request.getAddress());
        patient.setContactNumber(request.getContactNumber());
        patient.setActive(true);
        patient.setCreatedAt(LocalDateTime.now());
        patient.setUpdatedAt(LocalDateTime.now());
        return patient;
    }
    
    /**
     * Create a Doctor
     * 
     * @param name Doctor name
     * @param email Doctor email
     * @param encodedPassword Already encoded password
     * @param specialization Doctor's specialization
     * @param hospitalId Hospital where doctor works
     * @return New Doctor instance
     */
    public Doctor createDoctor(String name, String email, String encodedPassword, 
                               String specialization, String hospitalId) {
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setEmail(email);
        doctor.setPassword(encodedPassword);
        doctor.setRole(UserRole.DOCTOR);
        doctor.setSpecialization(specialization);
        doctor.setHospitalId(hospitalId);
        doctor.setCreatedAt(LocalDateTime.now());
        doctor.setUpdatedAt(LocalDateTime.now());
        return doctor;
    }
    
    /**
     * Create a Staff member
     * 
     * @param name Staff name
     * @param email Staff email
     * @param encodedPassword Already encoded password
     * @param hospitalId Hospital where staff works
     * @return New Staff instance
     */
    public Staff createStaff(String name, String email, String encodedPassword, String hospitalId) {
        Staff staff = new Staff();
        staff.setName(name);
        staff.setEmail(email);
        staff.setPassword(encodedPassword);
        staff.setRole(UserRole.STAFF);
        staff.setHospitalId(hospitalId);
        staff.setCreatedAt(LocalDateTime.now());
        staff.setUpdatedAt(LocalDateTime.now());
        return staff;
    }
    
    /**
     * Create a generic admin User
     * 
     * @param name User name
     * @param email User email
     * @param encodedPassword Already encoded password
     * @return New User instance
     */
    public User createAdmin(String name, String email, String encodedPassword) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole(UserRole.ADMIN);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
