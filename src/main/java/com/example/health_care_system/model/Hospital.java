package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "hospitals")
public class Hospital {
    
    @Id
    private String id;
    
    private String name;
    
    private HospitalType type;
    
    private Location location;
    
    private ContactInfo contactInfo;
    
    // Fixed amount charge for this hospital. For GOVERNMENT type this should be zero.
    private BigDecimal hospitalCharges = BigDecimal.ZERO;
    
    // Reference to doctors working at this hospital (lazy loaded)
    @DBRef(lazy = true)
    private List<Doctor> doctors = new ArrayList<>();
    
    // Reference to patients registered at this hospital (lazy loaded)
    @DBRef(lazy = true)
    private List<Patient> patients = new ArrayList<>();
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Enum for Hospital Type
    public enum HospitalType {
        GOVERNMENT,
        PRIVATE
    }
    
    // Nested class for Location
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private String address;
        private String city;
        private String state;
    }
    
    // Nested class for Contact Information
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactInfo {
        private String phoneNumber;
        private String email;
        private String website;
    }
    
    // Business Methods
    
    /**
     * Manage appointments for this hospital
     * @return List of appointments for all doctors in this hospital
     */
    public List<Appointment> manageAppointments() {
        List<Appointment> allAppointments = new ArrayList<>();
        for (Doctor doctor : doctors) {
            if (doctor.getAppointments() != null) {
                allAppointments.addAll(doctor.getAppointments());
            }
        }
        return allAppointments;
    }
    
    /**
     * Manage doctors at this hospital
     * @return List of doctors
     */
    public List<Doctor> manageDoctors() {
        return this.doctors;
    }
    
    /**
     * Add a doctor to the hospital
     * @param doctor Doctor to add
     */
    public void addDoctor(Doctor doctor) {
        if (!this.doctors.contains(doctor)) {
            this.doctors.add(doctor);
        }
    }
    
    /**
     * Remove a doctor from the hospital
     * @param doctor Doctor to remove
     */
    public void removeDoctor(Doctor doctor) {
        this.doctors.remove(doctor);
    }
    
    /**
     * Add a patient to the hospital
     * @param patient Patient to add
     */
    public void addPatient(Patient patient) {
        if (!this.patients.contains(patient)) {
            this.patients.add(patient);
        }
    }
    
    /**
     * Remove a patient from the hospital
     * @param patient Patient to remove
     */
    public void removePatient(Patient patient) {
        this.patients.remove(patient);
    }
}
