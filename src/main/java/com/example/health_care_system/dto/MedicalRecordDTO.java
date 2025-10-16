package com.example.health_care_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDTO {
    
    private String id;
    private String patientId;
    private String patientName;
    private LocalDate recordDate;
    private String diagnosis;
    private String prescription;
    private String doctorId;
    private String doctorName;
    private String notes;
}
