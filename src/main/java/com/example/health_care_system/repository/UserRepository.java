package com.example.health_care_system.repository;

import com.example.health_care_system.model.User;
import com.example.health_care_system.model.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    Optional<User> findByEmailAndRole(String email, UserRole role);
}
