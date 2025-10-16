package com.example.health_care_system.repository;

import com.example.health_care_system.model.HealthCard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthCardRepository extends MongoRepository<HealthCard, String> {
    
    Optional<HealthCard> findByPatientId(String patientId);
    
    boolean existsByPatientId(String patientId);
}
