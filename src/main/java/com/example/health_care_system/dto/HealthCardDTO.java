package com.example.health_care_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthCardDTO {
    private String id;
    private String patientId;
    private String patientName;
    private String qrCode;
    private String status;
    private LocalDate createDate;
    private LocalDate expireDate;
}
