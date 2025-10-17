package com.example.health_care_system.repository;

import com.example.health_care_system.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends MongoRepository<Patient, String> {
    Optional<Patient> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("{ 'role': 'PATIENT' }")
    List<Patient> findAll();
    
    @Query("{ 'role': 'PATIENT', '_id': ?0 }")
    Optional<Patient> findById(String id);
    
    @Query("{ 'role': 'PATIENT', 'email': ?0 }")
    Optional<Patient> findByEmailAndRole(String email);
}
