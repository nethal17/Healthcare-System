package com.example.health_care_system.repository;

import com.example.health_care_system.model.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {
    
    List<MedicalRecord> findByPatientId(String patientId);
    
    List<MedicalRecord> findByDoctorId(String doctorId);
    
    List<MedicalRecord> findByPatientIdOrderByRecordDateDesc(String patientId);
    
    List<MedicalRecord> findByRecordDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<MedicalRecord> findByPatientIdAndRecordDateBetween(String patientId, LocalDate startDate, LocalDate endDate);
}
