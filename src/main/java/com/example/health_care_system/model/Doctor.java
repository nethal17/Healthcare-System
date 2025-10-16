package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "users")
public class Doctor extends User {
    
    private String specialization;
    
    // Reference to the hospital where this doctor works
    private String hospitalId;
    
    // Reference to appointments (lazy loaded)
    @DBRef(lazy = true)
    private List<Appointment> appointments = new ArrayList<>();
    
    // Reference to medical records created by this doctor (lazy loaded)
    @DBRef(lazy = true)
    private List<MedicalRecord> medicalRecords = new ArrayList<>();
}
