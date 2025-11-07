package com.example.health_care_system.config;

import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class HospitalSeeder {

    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;

    @Bean
    @ConditionalOnProperty(name = "app.seeder.hospitals.enabled", havingValue = "true", matchIfMissing = false)
    public CommandLineRunner seedHospitals() {
        return args -> {
            log.info("Hospital seeder started...");

            // Hospital 1: City Medical Center
            if (!hospitalRepository.existsByName("City Medical Center")) {
                Hospital h1 = new Hospital();
                h1.setName("City Medical Center");
                h1.setType(Hospital.HospitalType.PRIVATE);
                h1.setHospitalCharges(new BigDecimal("5000.00"));

                Hospital.Location loc1 = new Hospital.Location();
                loc1.setAddress("123 Main Street");
                loc1.setCity("Colombo");
                loc1.setState("Western Province");
                h1.setLocation(loc1);

                Hospital.ContactInfo c1 = new Hospital.ContactInfo();
                c1.setPhoneNumber("0112345678");
                c1.setEmail("info@citymedical.lk");
                c1.setWebsite("www.citymedical.lk");
                h1.setContactInfo(c1);

                h1.setCreatedAt(LocalDateTime.now());
                h1.setUpdatedAt(LocalDateTime.now());

                Hospital savedH1 = hospitalRepository.save(h1);
                log.info("Created hospital: {}", savedH1.getName());

                // Optionally assign doctors with matching specializations
                List<Doctor> doctors = doctorRepository.findAll();
                for (Doctor d : doctors) {
                    if (d.getSpecialization() == null) continue;
                    String spec = d.getSpecialization().toLowerCase();
                    if (spec.contains("cardio") || spec.contains("derm") || spec.contains("orthop")) {
                        d.setHospitalId(savedH1.getId());
                        doctorRepository.save(d);
                        log.info("Assigned doctor {} to hospital {}", d.getEmail(), savedH1.getName());
                    }
                }
            } else {
                log.info("Hospital 'City Medical Center' already exists. Skipping.");
            }

            // Hospital 2: General Hospital
            if (!hospitalRepository.existsByName("General Hospital")) {
                Hospital h2 = new Hospital();
                h2.setName("General Hospital");
                h2.setType(Hospital.HospitalType.GOVERNMENT);
                h2.setHospitalCharges(BigDecimal.ZERO);

                Hospital.Location loc2 = new Hospital.Location();
                loc2.setAddress("456 Hospital Road");
                loc2.setCity("Kandy");
                loc2.setState("Central Province");
                h2.setLocation(loc2);

                Hospital.ContactInfo c2 = new Hospital.ContactInfo();
                c2.setPhoneNumber("0812345678");
                c2.setEmail("info@generalhospital.gov.lk");
                c2.setWebsite("www.generalhospital.gov.lk");
                h2.setContactInfo(c2);

                h2.setCreatedAt(LocalDateTime.now());
                h2.setUpdatedAt(LocalDateTime.now());

                Hospital savedH2 = hospitalRepository.save(h2);
                log.info("Created hospital: {}", savedH2.getName());

                // Assign doctors with matching specializations
                List<Doctor> doctors = doctorRepository.findAll();
                for (Doctor d : doctors) {
                    if (d.getSpecialization() == null) continue;
                    String spec = d.getSpecialization().toLowerCase();
                    if (spec.contains("pedi") || spec.contains("neuro") || spec.contains("general")) {
                        d.setHospitalId(savedH2.getId());
                        doctorRepository.save(d);
                        log.info("Assigned doctor {} to hospital {}", d.getEmail(), savedH2.getName());
                    }
                }
            } else {
                log.info("Hospital 'General Hospital' already exists. Skipping.");
            }

            log.info("Hospital seeder finished.");
        };
    }
}
