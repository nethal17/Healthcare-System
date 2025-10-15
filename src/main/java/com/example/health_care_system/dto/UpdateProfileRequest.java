package com.example.health_care_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String name;
    private String contactNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
}
