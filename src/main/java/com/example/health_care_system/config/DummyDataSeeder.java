package com.example.health_care_system.config;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.User;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

// Seeder disabled — removed @Component so it won't be registered as a Spring bean.
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = false)
public class DummyDataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DummyDataSeeder.class);

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public DummyDataSeeder(UserRepository userRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Running DummyDataSeeder (app.seeder.enabled=true)");

        // Admin
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = new User();
            admin.setId(UUID.randomUUID().toString());
            admin.setName("System Administrator");
            admin.setEmail("admin@example.com");
            admin.setPassword("password"); // change to hashed in prod
            admin.setRole(UserRole.ADMIN);
            admin.setContactNumber("+94111234567");
            userRepository.save(admin);
            logger.info("Created admin user");
        }

        // Doctor (as subtype)
        if (userRepository.findByEmail("saman.perera@example.com").isEmpty()) {
            Doctor doctor = new Doctor();
            doctor.setId(UUID.randomUUID().toString());
            doctor.setName("Dr. Saman Perera");
            doctor.setEmail("saman.perera@example.com");
            doctor.setPassword("password");
            doctor.setRole(UserRole.DOCTOR);
            doctor.setContactNumber("+94771122334");
            doctor.setSpecialization("General Practitioner");
            doctor.setHospitalId(null);
            doctorRepository.save(doctor);
            logger.info("Created doctor user");
        }

        // Staff
        if (userRepository.findByEmail("nimal.fernando@example.com").isEmpty()) {
            User staff = new User();
            staff.setId(UUID.randomUUID().toString());
            staff.setName("Nimal Fernando");
            staff.setEmail("nimal.fernando@example.com");
            staff.setPassword("password");
            staff.setRole(UserRole.STAFF);
            staff.setContactNumber("+94771234567");
            userRepository.save(staff);
            logger.info("Created staff user");
        }

        // Patient
        if (userRepository.findByEmail("kumari.silva@example.com").isEmpty()) {
            Patient patient = new Patient();
            patient.setId(UUID.randomUUID().toString());
            patient.setName("Kumari Silva");
            patient.setEmail("kumari.silva@example.com");
            patient.setPassword("password");
            patient.setRole(UserRole.PATIENT);
            patient.setContactNumber("+94779876543");
            patientRepository.save(patient);
            logger.info("Created patient user");
        }

        // Create sample appointments for today between the created doctor and patient
        // Find doctor/patient ids
        userRepository.findByEmailAndRole("saman.perera@example.com", UserRole.DOCTOR).ifPresent(user -> {
            String doctorId = user.getId();
            userRepository.findByEmailAndRole("kumari.silva@example.com", UserRole.PATIENT).ifPresent(p -> {
                String patientId = p.getId();

                // appointment at 10:00 today
                LocalDateTime today10 = LocalDateTime.now().with(LocalTime.of(10,0,0));
                boolean exists = appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(doctorId, today10.withHour(0), today10.withHour(23))
                        .stream().anyMatch(a -> a.getAppointmentDateTime().getHour() == 10);
                if (!exists) {
                    Appointment appt = new Appointment();
                    appt.setId(UUID.randomUUID().toString());
                    appt.setDoctorId(doctorId);
                    appt.setDoctorName(user.getName());
                    appt.setPatientId(patientId);
                    appt.setPatientName(p.getName());
                    appt.setAppointmentDateTime(today10);
                    appt.setStatus(Appointment.AppointmentStatus.CONFIRMED);
                    appointmentRepository.save(appt);
                    logger.info("Created sample appointment at 10:00");
                }

                // appointment at 11:00
                LocalDateTime today11 = LocalDateTime.now().with(LocalTime.of(11,0,0));
                boolean exists11 = appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(doctorId, today11.withHour(0), today11.withHour(23))
                        .stream().anyMatch(a -> a.getAppointmentDateTime().getHour() == 11);
                if (!exists11) {
                    Appointment appt2 = new Appointment();
                    appt2.setId(UUID.randomUUID().toString());
                    appt2.setDoctorId(doctorId);
                    appt2.setDoctorName(user.getName());
                    appt2.setPatientId(patientId);
                    appt2.setPatientName(p.getName());
                    appt2.setAppointmentDateTime(today11);
                    appt2.setStatus(Appointment.AppointmentStatus.CONFIRMED);
                    appointmentRepository.save(appt2);
                    logger.info("Created sample appointment at 11:00");
                }
            });
        });

        logger.info("DummyDataSeeder finished");
    }
}
