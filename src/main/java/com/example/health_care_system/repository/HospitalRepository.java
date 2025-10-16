package com.example.health_care_system.repository;

import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.model.Hospital.HospitalType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    
    Optional<Hospital> findByName(String name);
    
    List<Hospital> findByType(HospitalType type);
    
    List<Hospital> findByLocationCity(String city);
    
    List<Hospital> findByLocationState(String state);
    
    List<Hospital> findByLocationCityAndType(String city, HospitalType type);
    
    boolean existsByName(String name);
}
