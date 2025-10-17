package com.example.health_care_system.repository;

import com.example.health_care_system.model.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends MongoRepository<Doctor, String> {
    Optional<Doctor> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("{ 'role': 'DOCTOR' }")
    List<Doctor> findAll();
    
    @Query("{ 'role': 'DOCTOR', '_id': ?0 }")
    Optional<Doctor> findById(String id);
    
    @Query("{ 'role': 'DOCTOR', 'email': ?0 }")
    Optional<Doctor> findByEmailAndRole(String email);
    
    @Query("{ 'role': 'DOCTOR', 'hospitalId': ?0 }")
    List<Doctor> findByHospitalId(String hospitalId);
}
