package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("DOCTOR")
public class Doctor extends User {
    
    private String specialization;
    
    // Reference to the hospital where this doctor works
    private String hospitalId;
    
    // Reference to appointments (lazy loaded)
    @Transient
    private List<Appointment> appointments = new ArrayList<>();
    
    // Reference to medical records created by this doctor (lazy loaded)
    @Transient
    private List<MedicalRecord> medicalRecords = new ArrayList<>();
}
