package com.example.health_care_system.repository;

import com.example.health_care_system.model.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends MongoRepository<Doctor, String> {
    Optional<Doctor> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Doctor> findByHospitalId(String hospitalId);
    // findByDoctorId removed - use findById() instead (MongoDB ObjectId)
}
