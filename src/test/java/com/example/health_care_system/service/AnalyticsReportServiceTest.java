package com.example.health_care_system.service;

import com.example.health_care_system.model.*;
import com.example.health_care_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsReportServiceTest {

    @Mock
    private AnalyticsReportRepository analyticsReportRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private HospitalRepository hospitalRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    @Mock
    private UserRepository userRepository;

    private AnalyticsReportService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AnalyticsReportService();
        TestUtils.injectField(service, "analyticsReportRepository", analyticsReportRepository);
        TestUtils.injectField(service, "patientRepository", patientRepository);
        TestUtils.injectField(service, "doctorRepository", doctorRepository);
        TestUtils.injectField(service, "hospitalRepository", hospitalRepository);
        TestUtils.injectField(service, "appointmentRepository", appointmentRepository);
        TestUtils.injectField(service, "medicalRecordRepository", medicalRecordRepository);
        TestUtils.injectField(service, "userRepository", userRepository);
    }

    @Test
    void generatePatientReport_and_others_saveReport() {
        // Setup patients
        Patient p1 = new Patient(); p1.setId("p1"); p1.setName("P1"); p1.setGender("Male"); p1.setActive(true);
        when(patientRepository.findAll()).thenReturn(List.of(p1));
        when(analyticsReportRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var rpt = service.generatePatientReport("admin", LocalDateTime.now().minusDays(7), LocalDateTime.now());
        assertNotNull(rpt);
        verify(analyticsReportRepository).save(any());

        // Doctors
        Doctor d1 = new Doctor(); d1.setId("d1"); d1.setName("Dr"); d1.setSpecialization("S");
        when(doctorRepository.findAll()).thenReturn(List.of(d1));
        var drRpt = service.generateDoctorReport("admin", LocalDateTime.now().minusDays(7), LocalDateTime.now());
        assertNotNull(drRpt);

        // Hospitals
        Hospital h1 = new Hospital(); h1.setId("h1"); h1.setName("H1"); h1.setType(Hospital.HospitalType.PRIVATE);
        when(hospitalRepository.findAll()).thenReturn(List.of(h1));
        var hosRpt = service.generateHospitalReport("admin", LocalDateTime.now().minusDays(7), LocalDateTime.now());
        assertNotNull(hosRpt);

        // Appointments
        Appointment a1 = new Appointment(); a1.setId("a1"); a1.setAppointmentDateTime(LocalDateTime.now()); a1.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findAll()).thenReturn(List.of(a1));
        when(doctorRepository.findAll()).thenReturn(List.of(d1));
        var aptRpt = service.generateAppointmentReport("admin", LocalDateTime.now().minusDays(7), LocalDateTime.now());
        assertNotNull(aptRpt);

        // System overview
        when(userRepository.findAll()).thenReturn(List.of(new User()));
        when(patientRepository.count()).thenReturn(1L);
        when(doctorRepository.count()).thenReturn(1L);
        when(hospitalRepository.count()).thenReturn(1L);
        when(appointmentRepository.count()).thenReturn(1L);
        when(medicalRecordRepository.count()).thenReturn(1L);

        var sysRpt = service.generateSystemOverviewReport("admin");
        assertNotNull(sysRpt);
    }
}

