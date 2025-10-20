package com.example.health_care_system.service;

import com.example.health_care_system.model.*;
import com.example.health_care_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

        when(analyticsReportRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void generatePatientReport_happyPath() {
        Patient p1 = new Patient();
        p1.setId("p1");
        p1.setName("Alice");
        p1.setGender("Female");
        p1.setActive(true);
        p1.setDateOfBirth(LocalDate.now().minusYears(30));
        // appointments and medical records
        Appointment a = new Appointment(); a.setId("a1");
        p1.setAppointments(List.of(a));
        MedicalRecord r = new MedicalRecord(); r.setId("m1");
        p1.setMedicalRecords(List.of(r));
        p1.setHospitalId("h1");

        when(patientRepository.findAll()).thenReturn(List.of(p1));

        AnalyticsReport rpt = service.generatePatientReport("admin", LocalDateTime.now().minusDays(7), LocalDateTime.now());
        assertNotNull(rpt);
        assertEquals(1, rpt.getReportData().getPatientData().getTotalPatients());
        Map<String, Integer> byGender = rpt.getReportData().getPatientData().getPatientsByGender();
        assertTrue(byGender.containsKey("Female"));
        assertEquals(1, rpt.getReportData().getPatientData().getPatientsWithAppointments());
    }

    @Test
    void generateDoctorReport_happyPath() {
        Doctor d1 = new Doctor();
        d1.setId("d1"); d1.setName("Dr A"); d1.setSpecialization("Cardio");
        Appointment a1 = new Appointment(); a1.setId("a1"); a1.setStatus(Appointment.AppointmentStatus.COMPLETED);
        d1.setAppointments(List.of(a1));
        d1.setMedicalRecords(List.of(new MedicalRecord()));

        when(doctorRepository.findAll()).thenReturn(List.of(d1));

        AnalyticsReport rpt = service.generateDoctorReport("admin", LocalDateTime.now().minusDays(7), LocalDateTime.now());
        assertNotNull(rpt);
        assertEquals(1, rpt.getReportData().getDoctorData().getTotalDoctors());
        assertEquals(1, rpt.getReportData().getDoctorData().getTopDoctorsByAppointments().size());
    }

    @Test
    void generateHospitalReport_happyPath() {
        Hospital h = new Hospital(); h.setId("h1"); h.setName("H1"); h.setType(Hospital.HospitalType.PRIVATE);
        Doctor d = new Doctor(); d.setId("d1"); d.setName("Dr"); d.setHospitalId("h1");
        d.setAppointments(List.of(new Appointment()));
        h.setDoctors(List.of(d));
        h.setPatients(List.of(new Patient()));
        h.setHospitalCharges(new BigDecimal("500"));

        when(hospitalRepository.findAll()).thenReturn(List.of(h));

        AnalyticsReport rpt = service.generateHospitalReport("admin", LocalDateTime.now().minusDays(7), LocalDateTime.now());
        assertNotNull(rpt);
        assertEquals(1, rpt.getReportData().getHospitalData().getTotalHospitals());
        assertTrue(rpt.getReportData().getHospitalData().getHospitalMetrics().containsKey("h1"));
    }

    @Test
    void generateAppointmentReport_filtersAndAggregates() {
        Appointment a1 = new Appointment(); a1.setId("a1"); a1.setAppointmentDateTime(LocalDateTime.now()); a1.setStatus(Appointment.AppointmentStatus.SCHEDULED); a1.setDoctorId("d1"); a1.setDoctorName("Dr A");
        when(appointmentRepository.findAll()).thenReturn(List.of(a1));
        Doctor d = new Doctor(); d.setId("d1"); d.setName("Dr A"); d.setSpecialization("Cardio");
        when(doctorRepository.findAll()).thenReturn(List.of(d));

        AnalyticsReport rpt = service.generateAppointmentReport("admin", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertNotNull(rpt);
        assertEquals(1, rpt.getReportData().getAppointmentData().getTotalAppointments());
        assertTrue(rpt.getReportData().getAppointmentData().getAppointmentsByDoctor().containsKey("Dr A"));
    }

    @Test
    void generateSystemOverviewReport_aggregatesCounts() {
        User admin = new User(); admin.setRole(UserRole.ADMIN);
        User staff = new User(); staff.setRole(UserRole.STAFF);
        when(userRepository.findAll()).thenReturn(List.of(admin, staff));
        when(patientRepository.count()).thenReturn(2L);
        when(doctorRepository.count()).thenReturn(1L);
        when(hospitalRepository.count()).thenReturn(1L);
        when(appointmentRepository.count()).thenReturn(3L);
        when(medicalRecordRepository.count()).thenReturn(4L);

        AnalyticsReport rpt = service.generateSystemOverviewReport("admin");
        assertNotNull(rpt);
        // admin + staff from userRepository + patientCount + doctorCount => 1 + 1 + 2 + 1 = 5
        assertEquals(5, rpt.getReportData().getSystemData().getTotalUsers());

        // Basic sanity checks
        assertEquals((int) hospitalRepository.count(), rpt.getReportData().getSystemData().getTotalHospitals());
    }

    @Test
    void retrievalMethods_delegateToRepository() {
        AnalyticsReport r = new AnalyticsReport(); r.setReportId("R1");
        when(analyticsReportRepository.findAllByOrderByGeneratedAtDesc()).thenReturn(List.of(r));
        when(analyticsReportRepository.findByReportId("R1")).thenReturn(Optional.of(r));
        when(analyticsReportRepository.findByReportType(AnalyticsReport.ReportType.PATIENT_SUMMARY)).thenReturn(List.of(r));
        when(analyticsReportRepository.findByGeneratedBy("admin")).thenReturn(List.of(r));

        // Delegation checks
        List<AnalyticsReport> all = service.getAllReports();
        assertNotNull(all);
        assertEquals(1, all.size());
        assertEquals("R1", all.get(0).getReportId());

        Optional<AnalyticsReport> byId = service.getReportById("R1");
        assertTrue(byId.isPresent());
        assertEquals("R1", byId.get().getReportId());

        List<AnalyticsReport> byType = service.getReportsByType(AnalyticsReport.ReportType.PATIENT_SUMMARY);
        assertNotNull(byType);
        assertEquals(1, byType.size());

        List<AnalyticsReport> byUser = service.getReportsByUser("admin");
        assertNotNull(byUser);
        assertEquals(1, byUser.size());

        // Verify repository interactions
        verify(analyticsReportRepository).findAllByOrderByGeneratedAtDesc();
        verify(analyticsReportRepository).findByReportId("R1");
        verify(analyticsReportRepository).findByReportType(AnalyticsReport.ReportType.PATIENT_SUMMARY);
        verify(analyticsReportRepository).findByGeneratedBy("admin");
    }

    @Test
    void generatePatientReport_emptyRepositories_returnsZeroCounts() {
        when(patientRepository.findAll()).thenReturn(List.of());
        AnalyticsReport rpt = service.generatePatientReport("admin", java.time.LocalDateTime.now().minusDays(7), java.time.LocalDateTime.now());
        assertNotNull(rpt);
        assertEquals(0, rpt.getReportData().getPatientData().getTotalPatients());
    }

    @Test
    void generateAppointmentReport_invalidRange_handlesGracefully() {
        when(appointmentRepository.findAll()).thenReturn(List.of());
        // inverted dates
        AnalyticsReport rpt = service.generateAppointmentReport("admin", java.time.LocalDateTime.now().plusDays(1), java.time.LocalDateTime.now().minusDays(1));
        assertNotNull(rpt);
    }

}
