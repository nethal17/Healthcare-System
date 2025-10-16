package com.example.health_care_system.repository;

import com.example.health_care_system.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends MongoRepository<Patient, String> {
    Optional<Patient> findByEmail(String email);
    boolean existsByEmail(String email);
    // findByPatientId removed - use findById() instead (MongoDB ObjectId)
}
