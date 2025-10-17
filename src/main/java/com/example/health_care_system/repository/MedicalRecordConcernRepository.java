package com.example.health_care_system.repository;

import com.example.health_care_system.model.MedicalRecordConcern;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordConcernRepository extends MongoRepository<MedicalRecordConcern, String> {
    
    List<MedicalRecordConcern> findByPatientId(String patientId);
    
    List<MedicalRecordConcern> findByMedicalRecordId(String medicalRecordId);
    
    List<MedicalRecordConcern> findByStatus(String status);
    
    List<MedicalRecordConcern> findAllByOrderByCreatedAtDesc();
}
