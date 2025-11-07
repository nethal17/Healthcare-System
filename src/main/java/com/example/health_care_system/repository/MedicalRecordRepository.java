package com.example.health_care_system.repository;

import com.example.health_care_system.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, String> {
    
    List<MedicalRecord> findByPatientId(String patientId);
    
    List<MedicalRecord> findByDoctorId(String doctorId);
    
    List<MedicalRecord> findByPatientIdOrderByRecordDateDesc(String patientId);
    
    List<MedicalRecord> findByRecordDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<MedicalRecord> findByPatientIdAndRecordDateBetween(String patientId, LocalDate startDate, LocalDate endDate);
}
