package com.example.health_care_system.repository;

import com.example.health_care_system.model.Doctor;
<<<<<<< HEAD
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
=======
import org.springframework.data.jpa.repository.JpaRepository;
>>>>>>> 3ed1ba1 (Refactor application to use JPA with PostgreSQL)
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, String> {
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
<<<<<<< HEAD
=======
    // findByDoctorId removed - use findById()
>>>>>>> 3ed1ba1 (Refactor application to use JPA with PostgreSQL)
}
