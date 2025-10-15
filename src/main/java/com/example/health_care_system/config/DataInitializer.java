package com.example.health_care_system.config;

import com.example.health_care_system.model.User;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Create admin user if not exists
            if (!userRepository.existsByEmail("admin@healthcare.com")) {
                User admin = new User();
                admin.setName("Healthcare Manager");
                admin.setEmail("admin@healthcare.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(UserRole.ADMIN);
                admin.setGender("Male");
                admin.setContactNumber("0771234567");
                admin.setAddress("Hospital Main Building");
                admin.setDateOfBirth(LocalDate.of(1980, 1, 1));
                admin.setActive(true);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                userRepository.save(admin);
                log.info("Admin user created - Email: admin@healthcare.com, Password: admin123");
            }
            
            // Create sample doctor 1 if not exists
            if (!userRepository.existsByEmail("doctor1@healthcare.com")) {
                User doctor1 = new User();
                doctor1.setName("Dr. John Smith");
                doctor1.setEmail("doctor1@healthcare.com");
                doctor1.setPassword(passwordEncoder.encode("doctor123"));
                doctor1.setRole(UserRole.DOCTOR);
                doctor1.setGender("Male");
                doctor1.setContactNumber("0772345678");
                doctor1.setAddress("Cardiology Department");
                doctor1.setDateOfBirth(LocalDate.of(1975, 5, 15));
                doctor1.setActive(true);
                doctor1.setCreatedAt(LocalDateTime.now());
                doctor1.setUpdatedAt(LocalDateTime.now());
                userRepository.save(doctor1);
                log.info("Doctor 1 created - Email: doctor1@healthcare.com, Password: doctor123");
            }
            
            // Create sample doctor 2 if not exists
            if (!userRepository.existsByEmail("doctor2@healthcare.com")) {
                User doctor2 = new User();
                doctor2.setName("Dr. Sarah Johnson");
                doctor2.setEmail("doctor2@healthcare.com");
                doctor2.setPassword(passwordEncoder.encode("doctor123"));
                doctor2.setRole(UserRole.DOCTOR);
                doctor2.setGender("Female");
                doctor2.setContactNumber("0773456789");
                doctor2.setAddress("Pediatrics Department");
                doctor2.setDateOfBirth(LocalDate.of(1982, 8, 20));
                doctor2.setActive(true);
                doctor2.setCreatedAt(LocalDateTime.now());
                doctor2.setUpdatedAt(LocalDateTime.now());
                userRepository.save(doctor2);
                log.info("Doctor 2 created - Email: doctor2@healthcare.com, Password: doctor123");
            }
            
            log.info("Data initialization completed!");
        };
    }
}
