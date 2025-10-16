package com.example.health_care_system.config;

import com.example.health_care_system.model.*;
import com.example.health_care_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AnalyticsReportRepository analyticsReportRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Only initialize data if database is empty
            if (userRepository.count() > 0) {
                log.info("✅ Database already contains data. Skipping initialization.");
                return;
            }
            
            log.info("🚀 Initializing sample data...");
            
            // Create admin user
            if (!userRepository.existsByEmail("admin@healthcare.com")) {
                User admin = new User();
                admin.setName("Healthcare Manager");
                admin.setEmail("admin@healthcare.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(UserRole.ADMIN);
                admin.setGender("Male");
                admin.setContactNumber("0771234567");
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                userRepository.save(admin);
                log.info("Admin user created - Email: admin@healthcare.com, Password: admin123");
            }
            
            // Create sample doctor 1 if not exists
            if (!doctorRepository.existsByEmail("doctor1@healthcare.com")) {
                Doctor doctor1 = new Doctor();
                doctor1.setName("Dr. John Smith");
                doctor1.setEmail("doctor1@healthcare.com");
                doctor1.setPassword(passwordEncoder.encode("doctor123"));
                doctor1.setRole(UserRole.DOCTOR);
                doctor1.setGender("Male");
                doctor1.setContactNumber("0772345678");
                doctor1.setSpecialization("Cardiology");
                doctor1.setCreatedAt(LocalDateTime.now());
                doctor1.setUpdatedAt(LocalDateTime.now());
                doctorRepository.save(doctor1);
                log.info("Doctor 1 created - Email: doctor1@healthcare.com, Password: doctor123");
            }
            
            // Create sample doctor 2 if not exists
            if (!doctorRepository.existsByEmail("doctor2@healthcare.com")) {
                Doctor doctor2 = new Doctor();
                doctor2.setName("Dr. Sarah Johnson");
                doctor2.setEmail("doctor2@healthcare.com");
                doctor2.setPassword(passwordEncoder.encode("doctor123"));
                doctor2.setRole(UserRole.DOCTOR);
                doctor2.setGender("Female");
                doctor2.setContactNumber("0773456789");
                doctor2.setSpecialization("Pediatrics");
                doctor2.setCreatedAt(LocalDateTime.now());
                doctor2.setUpdatedAt(LocalDateTime.now());
                doctorRepository.save(doctor2);
                log.info("Doctor 2 created - Email: doctor2@healthcare.com, Password: doctor123");
            }
            
            // Create sample doctor 3 - Dermatology
            if (!doctorRepository.existsByEmail("doctor3@healthcare.com")) {
                Doctor doctor3 = new Doctor();
                doctor3.setName("Dr. Michael Chen");
                doctor3.setEmail("doctor3@healthcare.com");
                doctor3.setPassword(passwordEncoder.encode("doctor123"));
                doctor3.setRole(UserRole.DOCTOR);
                doctor3.setGender("Male");
                doctor3.setContactNumber("0774567890");
                doctor3.setSpecialization("Dermatology");
                doctor3.setCreatedAt(LocalDateTime.now());
                doctor3.setUpdatedAt(LocalDateTime.now());
                doctorRepository.save(doctor3);
                log.info("Doctor 3 created - Email: doctor3@healthcare.com, Password: doctor123");
            }
            
            // Create sample doctor 4 - Orthopedics
            if (!doctorRepository.existsByEmail("doctor4@healthcare.com")) {
                Doctor doctor4 = new Doctor();
                doctor4.setName("Dr. Emily Rodriguez");
                doctor4.setEmail("doctor4@healthcare.com");
                doctor4.setPassword(passwordEncoder.encode("doctor123"));
                doctor4.setRole(UserRole.DOCTOR);
                doctor4.setGender("Female");
                doctor4.setContactNumber("0775678901");
                doctor4.setSpecialization("Orthopedics");
                doctor4.setCreatedAt(LocalDateTime.now());
                doctor4.setUpdatedAt(LocalDateTime.now());
                doctorRepository.save(doctor4);
                log.info("Doctor 4 created - Email: doctor4@healthcare.com, Password: doctor123");
            }
            
            // Create sample doctor 5 - Neurology
            if (!doctorRepository.existsByEmail("doctor5@healthcare.com")) {
                Doctor doctor5 = new Doctor();
                doctor5.setName("Dr. David Kumar");
                doctor5.setEmail("doctor5@healthcare.com");
                doctor5.setPassword(passwordEncoder.encode("doctor123"));
                doctor5.setRole(UserRole.DOCTOR);
                doctor5.setGender("Male");
                doctor5.setContactNumber("0776789012");
                doctor5.setSpecialization("Neurology");
                doctor5.setCreatedAt(LocalDateTime.now());
                doctor5.setUpdatedAt(LocalDateTime.now());
                doctorRepository.save(doctor5);
                log.info("Doctor 5 created - Email: doctor5@healthcare.com, Password: doctor123");
            }
            
            // Create sample doctor 6 - General Medicine
            if (!doctorRepository.existsByEmail("doctor6@healthcare.com")) {
                Doctor doctor6 = new Doctor();
                doctor6.setName("Dr. Priya Patel");
                doctor6.setEmail("doctor6@healthcare.com");
                doctor6.setPassword(passwordEncoder.encode("doctor123"));
                doctor6.setRole(UserRole.DOCTOR);
                doctor6.setGender("Female");
                doctor6.setContactNumber("0777890123");
                doctor6.setSpecialization("General Medicine");
                doctor6.setCreatedAt(LocalDateTime.now());
                doctor6.setUpdatedAt(LocalDateTime.now());
                doctorRepository.save(doctor6);
                log.info("Doctor 6 created - Email: doctor6@healthcare.com, Password: doctor123");
            }
            
            // Create sample doctor 7 - Oncology
            if (!doctorRepository.existsByEmail("doctor7@healthcare.com")) {
                Doctor doctor7 = new Doctor();
                doctor7.setName("Dr. Robert Williams");
                doctor7.setEmail("doctor7@healthcare.com");
                doctor7.setPassword(passwordEncoder.encode("doctor123"));
                doctor7.setRole(UserRole.DOCTOR);
                doctor7.setGender("Male");
                doctor7.setContactNumber("0778901234");
                doctor7.setSpecialization("Oncology");
                doctor7.setCreatedAt(LocalDateTime.now());
                doctor7.setUpdatedAt(LocalDateTime.now());
                doctorRepository.save(doctor7);
                log.info("Doctor 7 created - Email: doctor7@healthcare.com, Password: doctor123");
            }
            
            // Create sample doctor 8 - Psychiatry
            if (!doctorRepository.existsByEmail("doctor8@healthcare.com")) {
                Doctor doctor8 = new Doctor();
                doctor8.setName("Dr. Lisa Anderson");
                doctor8.setEmail("doctor8@healthcare.com");
                doctor8.setPassword(passwordEncoder.encode("doctor123"));
                doctor8.setRole(UserRole.DOCTOR);
                doctor8.setGender("Female");
                doctor8.setContactNumber("0779012345");
                doctor8.setSpecialization("Psychiatry");
                doctor8.setCreatedAt(LocalDateTime.now());
                doctor8.setUpdatedAt(LocalDateTime.now());
                doctorRepository.save(doctor8);
                log.info("Doctor 8 created - Email: doctor8@healthcare.com, Password: doctor123");
            }
            
            // Create sample staff member if not exists
            if (!userRepository.existsByEmail("staff@healthcare.com")) {
                User staff = new User();
                staff.setName("Reception Staff");
                staff.setEmail("staff@healthcare.com");
                staff.setPassword(passwordEncoder.encode("staff123"));
                staff.setRole(UserRole.STAFF);
                staff.setGender("Female");
                staff.setContactNumber("0774567890");
                staff.setCreatedAt(LocalDateTime.now());
                staff.setUpdatedAt(LocalDateTime.now());
                userRepository.save(staff);
                log.info("Staff member created - Email: staff@healthcare.com, Password: staff123");
            }
            
            // Create sample hospitals with fresh data
            createSampleHospitals();
            
            log.info("Data initialization completed!");
        };
    }
    
    /**
     * Clear all data from the database to ensure fresh start with MongoDB ObjectIds
     * NOTE: This method is kept for manual testing/debugging purposes but not called automatically
     * To clear database, uncomment the clearAllData() call in initializeData() method
     */
    @SuppressWarnings("unused")
    private void clearAllData() {
        log.info("🗑️  Clearing all existing data from database...");
        
        // Clear all collections in the correct order (to avoid reference issues)
        analyticsReportRepository.deleteAll();
        log.info("   ✓ Cleared analytics reports");
        
        medicalRecordRepository.deleteAll();
        log.info("   ✓ Cleared medical records");
        
        appointmentRepository.deleteAll();
        log.info("   ✓ Cleared appointments");
        
        patientRepository.deleteAll();
        log.info("   ✓ Cleared patients");
        
        doctorRepository.deleteAll();
        log.info("   ✓ Cleared doctors");
        
        hospitalRepository.deleteAll();
        log.info("   ✓ Cleared hospitals");
        
        userRepository.deleteAll();
        log.info("   ✓ Cleared users");
        
        log.info("✅ Database cleared successfully! Ready to insert fresh data with MongoDB ObjectIds.");
    }
    
    private void createSampleHospitals() {
        log.info("🏥 Creating sample hospitals...");
        
        // Get all doctors to assign to hospitals (filter only users with DOCTOR role)
        java.util.List<Doctor> allDoctors = doctorRepository.findAll().stream()
                .filter(doctor -> doctor.getRole() == UserRole.DOCTOR)
                .toList();
        
        // Hospital 1: City Medical Center (Private)
        Hospital hospital1 = new Hospital();
        hospital1.setName("City Medical Center");
        hospital1.setType(Hospital.HospitalType.PRIVATE);
    // Private hospitals charge a fixed amount
    hospital1.setHospitalCharges(new java.math.BigDecimal("5000.00"));
        
        Hospital.Location location1 = new Hospital.Location();
        location1.setAddress("123 Main Street");
        location1.setCity("Colombo");
        location1.setState("Western Province");
        hospital1.setLocation(location1);
        
        Hospital.ContactInfo contact1 = new Hospital.ContactInfo();
        contact1.setPhoneNumber("0112345678");
        contact1.setEmail("info@citymedical.lk");
        contact1.setWebsite("www.citymedical.lk");
        hospital1.setContactInfo(contact1);
        
        hospital1.setDoctors(new ArrayList<>());
        hospital1.setCreatedAt(LocalDateTime.now());
        hospital1.setUpdatedAt(LocalDateTime.now());
        
        Hospital savedHospital1 = hospitalRepository.save(hospital1);
        
        // Add doctors to City Medical Center (Private Hospital)
        // Specializations: Cardiology, Dermatology, Orthopedics
        savedHospital1.getDoctors().clear();
        if (allDoctors.size() >= 3) {
            Doctor doc1 = allDoctors.get(0); // Cardiology
            doc1.setHospitalId(savedHospital1.getId());  // Use hospital's MongoDB ObjectId
            doctorRepository.save(doc1);
            savedHospital1.getDoctors().add(doc1);
            
            Doctor doc2 = allDoctors.get(2); // Dermatology
            doc2.setHospitalId(savedHospital1.getId());  // Use hospital's MongoDB ObjectId
            doctorRepository.save(doc2);
            savedHospital1.getDoctors().add(doc2);
            
            Doctor doc3 = allDoctors.get(3); // Orthopedics
            doc3.setHospitalId(savedHospital1.getId());  // Use hospital's MongoDB ObjectId
            doctorRepository.save(doc3);
            savedHospital1.getDoctors().add(doc3);
        }
        hospitalRepository.save(savedHospital1);
        log.info("   ✓ City Medical Center created with " + savedHospital1.getDoctors().size() + " doctors");
        
        // Hospital 2: General Hospital (Government)
        Hospital hospital2 = new Hospital();
        hospital2.setName("General Hospital");
        hospital2.setType(Hospital.HospitalType.GOVERNMENT);
    // Government hospitals do not charge
    hospital2.setHospitalCharges(java.math.BigDecimal.ZERO);
        
        Hospital.Location location2 = new Hospital.Location();
        location2.setAddress("456 Hospital Road");
        location2.setCity("Kandy");
        location2.setState("Central Province");
        hospital2.setLocation(location2);
        
        Hospital.ContactInfo contact2 = new Hospital.ContactInfo();
        contact2.setPhoneNumber("0812345678");
        contact2.setEmail("info@generalhospital.gov.lk");
        contact2.setWebsite("www.generalhospital.gov.lk");
        hospital2.setContactInfo(contact2);
        
        hospital2.setDoctors(new ArrayList<>());
        hospital2.setCreatedAt(LocalDateTime.now());
        hospital2.setUpdatedAt(LocalDateTime.now());
        
        Hospital savedHospital2 = hospitalRepository.save(hospital2);
        
        // Add doctors to General Hospital (Government Hospital)
        // Specializations: Pediatrics, Neurology, General Medicine
        savedHospital2.getDoctors().clear();
        if (allDoctors.size() >= 6) {
            Doctor doc1 = allDoctors.get(1); // Pediatrics
            doc1.setHospitalId(savedHospital2.getId());  // Use hospital's MongoDB ObjectId
            doctorRepository.save(doc1);
            savedHospital2.getDoctors().add(doc1);
            
            Doctor doc2 = allDoctors.get(4); // Neurology
            doc2.setHospitalId(savedHospital2.getId());  // Use hospital's MongoDB ObjectId
            doctorRepository.save(doc2);
            savedHospital2.getDoctors().add(doc2);
            
            Doctor doc3 = allDoctors.get(5); // General Medicine
            doc3.setHospitalId(savedHospital2.getId());  // Use hospital's MongoDB ObjectId
            doctorRepository.save(doc3);
            savedHospital2.getDoctors().add(doc3);
        }
        hospitalRepository.save(savedHospital2);
        log.info("   ✓ General Hospital created with " + savedHospital2.getDoctors().size() + " doctors");
        
        // Hospital 3: National Hospital (Government - Largest)
        Hospital hospital3 = new Hospital();
        hospital3.setName("National Hospital");
        hospital3.setType(Hospital.HospitalType.GOVERNMENT);
    // Government hospitals do not charge
    hospital3.setHospitalCharges(java.math.BigDecimal.ZERO);
        
        Hospital.Location location3 = new Hospital.Location();
        location3.setAddress("789 Regent Street");
        location3.setCity("Colombo");
        location3.setState("Western Province");
        hospital3.setLocation(location3);
        
        Hospital.ContactInfo contact3 = new Hospital.ContactInfo();
        contact3.setPhoneNumber("0114567890");
        contact3.setEmail("info@nationalhospital.gov.lk");
        contact3.setWebsite("www.nationalhospital.gov.lk");
        hospital3.setContactInfo(contact3);
        
        hospital3.setDoctors(new ArrayList<>());
        hospital3.setCreatedAt(LocalDateTime.now());
        hospital3.setUpdatedAt(LocalDateTime.now());
        
        Hospital savedHospital3 = hospitalRepository.save(hospital3);
        
        // Add remaining doctors to National Hospital (Largest Government Hospital)
        // Specializations: Oncology, Psychiatry
        savedHospital3.getDoctors().clear();
        if (allDoctors.size() >= 8) {
            Doctor doc1 = allDoctors.get(6); // Oncology
            doc1.setHospitalId(savedHospital3.getId());  // Use hospital's MongoDB ObjectId
            doctorRepository.save(doc1);
            savedHospital3.getDoctors().add(doc1);
            
            Doctor doc2 = allDoctors.get(7); // Psychiatry
            doc2.setHospitalId(savedHospital3.getId());  // Use hospital's MongoDB ObjectId
            doctorRepository.save(doc2);
            savedHospital3.getDoctors().add(doc2);
        }
        hospitalRepository.save(savedHospital3);
        log.info("   ✓ National Hospital created with " + savedHospital3.getDoctors().size() + " doctors");
        
        log.info("✅ All hospitals created successfully!");
    }
}
