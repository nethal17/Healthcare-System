package com.example.health_care_system.mapper;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Staff;
import com.example.health_care_system.model.User;
import com.example.health_care_system.service.HealthCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper class responsible for converting User entities to UserDTO.
 * Follows Single Responsibility Principle - only handles DTO mapping.
 * Improves code reusability and testability.
 */
@Component
@RequiredArgsConstructor
public class UserMapper {
    
    private final HealthCardService healthCardService;
    
    /**
     * Convert User entity to UserDTO
     */
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setGender(user.getGender());
        dto.setContactNumber(user.getContactNumber());
        
        // Handle Patient-specific fields
        if (user instanceof Patient patient) {
            mapPatientFields(patient, dto);
        }
        
        // Handle Doctor-specific fields
        if (user instanceof Doctor doctor) {
            mapDoctorFields(doctor, dto);
        }
        
        // Handle Staff-specific fields
        if (user instanceof Staff staff) {
            mapStaffFields(staff, dto);
        }
        
        return dto;
    }
    
    /**
     * Map patient-specific fields to DTO
     */
    private void mapPatientFields(Patient patient, UserDTO dto) {
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setBloodType(patient.getBloodType());
        dto.setAddress(patient.getAddress());
        dto.setQrCode(patient.getQrCode());
        
        // Include health card information if available
        healthCardService.getHealthCardByPatientId(patient.getId())
            .ifPresent(healthCard -> 
                dto.setHealthCard(healthCardService.convertToDTO(healthCard))
            );
    }
    
    /**
     * Map doctor-specific fields to DTO
     */
    private void mapDoctorFields(Doctor doctor, UserDTO dto) {
        dto.setHospitalId(doctor.getHospitalId());
    }
    
    /**
     * Map staff-specific fields to DTO
     */
    private void mapStaffFields(Staff staff, UserDTO dto) {
        dto.setHospitalId(staff.getHospitalId());
    }
}
