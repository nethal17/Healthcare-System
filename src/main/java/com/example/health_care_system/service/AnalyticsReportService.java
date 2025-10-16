package com.example.health_care_system.service;

import com.example.health_care_system.model.*;
import com.example.health_care_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsReportService {
    
    @Autowired
    private AnalyticsReportRepository analyticsReportRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private HospitalRepository hospitalRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Generate a comprehensive patient report
     */
    public AnalyticsReport generatePatientReport(String generatedBy, LocalDateTime periodStart, LocalDateTime periodEnd) {
        AnalyticsReport report = new AnalyticsReport();
        report.setReportId(generateReportId("PAT"));
        report.setReportType(AnalyticsReport.ReportType.PATIENT_SUMMARY);
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportPeriodStart(periodStart);
        report.setReportPeriodEnd(periodEnd);
        report.setGeneratedBy(generatedBy);
        
        AnalyticsReport.ReportData reportData = new AnalyticsReport.ReportData();
        AnalyticsReport.PatientReportData patientData = new AnalyticsReport.PatientReportData();
        
        List<Patient> allPatients = patientRepository.findAll();
        
        // Total patients
        patientData.setTotalPatients(allPatients.size());
        
        // Active vs Inactive patients
        long activeCount = allPatients.stream().filter(Patient::isActive).count();
        patientData.setActivePatients((int) activeCount);
        patientData.setInactivePatients((int) (allPatients.size() - activeCount));
        
        // Patients by gender
        Map<String, Integer> byGender = allPatients.stream()
            .collect(Collectors.groupingBy(
                p -> p.getGender() != null ? p.getGender() : "Not Specified",
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        patientData.setPatientsByGender(byGender);
        
        // Patients by age group
        Map<String, Integer> byAgeGroup = allPatients.stream()
            .collect(Collectors.groupingBy(
                this::getAgeGroup,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        patientData.setPatientsByAgeGroup(byAgeGroup);
        
        // Patients by hospital
        Map<String, Integer> byHospital = allPatients.stream()
            .filter(p -> p.getHospitalId() != null)
            .collect(Collectors.groupingBy(
                Patient::getHospitalId,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        patientData.setPatientsByHospital(byHospital);
        
        // Patients with appointments and medical records
        long patientsWithAppointments = allPatients.stream()
            .filter(p -> p.getAppointments() != null && !p.getAppointments().isEmpty())
            .count();
        patientData.setPatientsWithAppointments((int) patientsWithAppointments);
        
        long patientsWithRecords = allPatients.stream()
            .filter(p -> p.getMedicalRecords() != null && !p.getMedicalRecords().isEmpty())
            .count();
        patientData.setPatientsWithMedicalRecords((int) patientsWithRecords);
        
        // Top patients by visits
        List<AnalyticsReport.PatientDetail> topPatients = allPatients.stream()
            .map(this::createPatientDetail)
            .sorted((p1, p2) -> Integer.compare(p2.getTotalVisits(), p1.getTotalVisits()))
            .limit(10)
            .collect(Collectors.toList());
        patientData.setTopPatientsByVisits(topPatients);
        
        reportData.setPatientData(patientData);
        report.setReportData(reportData);
        
        return analyticsReportRepository.save(report);
    }
    
    /**
     * Generate a comprehensive doctor performance report
     */
    public AnalyticsReport generateDoctorReport(String generatedBy, LocalDateTime periodStart, LocalDateTime periodEnd) {
        AnalyticsReport report = new AnalyticsReport();
        report.setReportId(generateReportId("DOC"));
        report.setReportType(AnalyticsReport.ReportType.DOCTOR_PERFORMANCE);
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportPeriodStart(periodStart);
        report.setReportPeriodEnd(periodEnd);
        report.setGeneratedBy(generatedBy);
        
        AnalyticsReport.ReportData reportData = new AnalyticsReport.ReportData();
        AnalyticsReport.DoctorReportData doctorData = new AnalyticsReport.DoctorReportData();
        
        List<Doctor> allDoctors = doctorRepository.findAll();
        
        // Total doctors
        doctorData.setTotalDoctors(allDoctors.size());
        doctorData.setActiveDoctors(allDoctors.size()); // Assuming all doctors are active
        
        // Doctors by specialization
        Map<String, Integer> bySpecialization = allDoctors.stream()
            .collect(Collectors.groupingBy(
                d -> d.getSpecialization() != null ? d.getSpecialization() : "Not Specified",
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        doctorData.setDoctorsBySpecialization(bySpecialization);
        
        // Doctors by hospital
        Map<String, Integer> byHospital = allDoctors.stream()
            .filter(d -> d.getHospitalId() != null)
            .collect(Collectors.groupingBy(
                Doctor::getHospitalId,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        doctorData.setDoctorsByHospital(byHospital);
        
        // Doctors by gender
        Map<String, Integer> byGender = allDoctors.stream()
            .collect(Collectors.groupingBy(
                d -> d.getGender() != null ? d.getGender() : "Not Specified",
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        doctorData.setDoctorsByGender(byGender);
        
        // Top doctors by appointments
        List<AnalyticsReport.DoctorPerformance> topDoctors = allDoctors.stream()
            .map(this::createDoctorPerformance)
            .sorted((d1, d2) -> Integer.compare(d2.getTotalAppointments(), d1.getTotalAppointments()))
            .limit(10)
            .collect(Collectors.toList());
        doctorData.setTopDoctorsByAppointments(topDoctors);
        
        // Average appointments per doctor
        double avgAppointments = allDoctors.stream()
            .mapToInt(d -> d.getAppointments() != null ? d.getAppointments().size() : 0)
            .average()
            .orElse(0.0);
        doctorData.setAverageAppointmentsPerDoctor(avgAppointments);
        
        // Doctors with medical records
        long doctorsWithRecords = allDoctors.stream()
            .filter(d -> d.getMedicalRecords() != null && !d.getMedicalRecords().isEmpty())
            .count();
        doctorData.setDoctorsWithMedicalRecords((int) doctorsWithRecords);
        
        reportData.setDoctorData(doctorData);
        report.setReportData(reportData);
        
        return analyticsReportRepository.save(report);
    }
    
    /**
     * Generate a comprehensive hospital overview report
     */
    public AnalyticsReport generateHospitalReport(String generatedBy, LocalDateTime periodStart, LocalDateTime periodEnd) {
        AnalyticsReport report = new AnalyticsReport();
        report.setReportId(generateReportId("HOS"));
        report.setReportType(AnalyticsReport.ReportType.HOSPITAL_OVERVIEW);
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportPeriodStart(periodStart);
        report.setReportPeriodEnd(periodEnd);
        report.setGeneratedBy(generatedBy);
        
        AnalyticsReport.ReportData reportData = new AnalyticsReport.ReportData();
        AnalyticsReport.HospitalReportData hospitalData = new AnalyticsReport.HospitalReportData();
        
        List<Hospital> allHospitals = hospitalRepository.findAll();
        
        // Total hospitals
        hospitalData.setTotalHospitals(allHospitals.size());
        
        // Government vs Private
        long govCount = allHospitals.stream()
            .filter(h -> h.getType() == Hospital.HospitalType.GOVERNMENT)
            .count();
        hospitalData.setGovernmentHospitals((int) govCount);
        hospitalData.setPrivateHospitals((int) (allHospitals.size() - govCount));
        
        // Hospitals by location
        Map<String, Integer> byLocation = allHospitals.stream()
            .filter(h -> h.getLocation() != null && h.getLocation().getCity() != null)
            .collect(Collectors.groupingBy(
                h -> h.getLocation().getCity(),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        hospitalData.setHospitalsByLocation(byLocation);
        
        // Hospital metrics (using MongoDB ObjectId as key)
        Map<String, AnalyticsReport.HospitalMetrics> metricsMap = allHospitals.stream()
            .collect(Collectors.toMap(
                h -> h.getId(),  // Use MongoDB ObjectId
                this::createHospitalMetrics
            ));
        hospitalData.setHospitalMetrics(metricsMap);
        
        // Total doctors and patients across all hospitals
        int totalDoctors = allHospitals.stream()
            .mapToInt(h -> h.getDoctors() != null ? h.getDoctors().size() : 0)
            .sum();
        hospitalData.setTotalDoctorsAcrossHospitals(totalDoctors);
        
        int totalPatients = allHospitals.stream()
            .mapToInt(h -> h.getPatients() != null ? h.getPatients().size() : 0)
            .sum();
        hospitalData.setTotalPatientsAcrossHospitals(totalPatients);
        
        // Averages
        hospitalData.setAverageDoctorsPerHospital(
            allHospitals.isEmpty() ? 0.0 : (double) totalDoctors / allHospitals.size()
        );
        hospitalData.setAveragePatientsPerHospital(
            allHospitals.isEmpty() ? 0.0 : (double) totalPatients / allHospitals.size()
        );
        
        reportData.setHospitalData(hospitalData);
        report.setReportData(reportData);
        
        return analyticsReportRepository.save(report);
    }
    
    /**
     * Generate a comprehensive appointment statistics report
     */
    public AnalyticsReport generateAppointmentReport(String generatedBy, LocalDateTime periodStart, LocalDateTime periodEnd) {
        AnalyticsReport report = new AnalyticsReport();
        report.setReportId(generateReportId("APT"));
        report.setReportType(AnalyticsReport.ReportType.APPOINTMENT_STATISTICS);
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportPeriodStart(periodStart);
        report.setReportPeriodEnd(periodEnd);
        report.setGeneratedBy(generatedBy);
        
        AnalyticsReport.ReportData reportData = new AnalyticsReport.ReportData();
        AnalyticsReport.AppointmentReportData appointmentData = new AnalyticsReport.AppointmentReportData();
        
        List<Appointment> allAppointments = appointmentRepository.findAll();
        
        // Filter by period if specified
        if (periodStart != null && periodEnd != null) {
            allAppointments = allAppointments.stream()
                .filter(a -> a.getAppointmentDateTime() != null &&
                    !a.getAppointmentDateTime().isBefore(periodStart) &&
                    !a.getAppointmentDateTime().isAfter(periodEnd))
                .collect(Collectors.toList());
        }
        
        // Total appointments
        appointmentData.setTotalAppointments(allAppointments.size());
        
        // Appointments by status
        Map<Appointment.AppointmentStatus, Long> byStatus = allAppointments.stream()
            .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
        
        appointmentData.setScheduledAppointments(byStatus.getOrDefault(Appointment.AppointmentStatus.SCHEDULED, 0L).intValue());
        appointmentData.setCompletedAppointments(byStatus.getOrDefault(Appointment.AppointmentStatus.COMPLETED, 0L).intValue());
        appointmentData.setCancelledAppointments(byStatus.getOrDefault(Appointment.AppointmentStatus.CANCELLED, 0L).intValue());
        appointmentData.setNoShowAppointments(byStatus.getOrDefault(Appointment.AppointmentStatus.NO_SHOW, 0L).intValue());
        
        // Appointments by doctor
        Map<String, Integer> byDoctor = allAppointments.stream()
            .collect(Collectors.groupingBy(
                a -> a.getDoctorName() != null ? a.getDoctorName() : "Unknown",
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        appointmentData.setAppointmentsByDoctor(byDoctor);
        
        // Appointments by specialization (get from doctor, using MongoDB ObjectId)
        Map<String, Integer> bySpecialization = new HashMap<>();
        List<Doctor> allDoctors = doctorRepository.findAll();
        for (Doctor doctor : allDoctors) {
            long count = allAppointments.stream()
                .filter(a -> a.getDoctorId() != null && a.getDoctorId().equals(doctor.getId()))  // Use MongoDB ObjectId
                .count();
            if (count > 0) {
                String spec = doctor.getSpecialization() != null ? doctor.getSpecialization() : "Not Specified";
                bySpecialization.put(spec, bySpecialization.getOrDefault(spec, 0) + (int) count);
            }
        }
        appointmentData.setAppointmentsBySpecialization(bySpecialization);
        
        // Appointments by hospital (get from doctor, using MongoDB ObjectId)
        Map<String, Integer> byHospital = new HashMap<>();
        for (Doctor doctor : allDoctors) {
            if (doctor.getHospitalId() != null) {
                long count = allAppointments.stream()
                    .filter(a -> a.getDoctorId() != null && a.getDoctorId().equals(doctor.getId()))  // Use MongoDB ObjectId
                    .count();
                if (count > 0) {
                    byHospital.put(doctor.getHospitalId(), byHospital.getOrDefault(doctor.getHospitalId(), 0) + (int) count);
                }
            }
        }
        appointmentData.setAppointmentsByHospital(byHospital);
        
        // Appointments by date
        Map<String, Integer> byDate = allAppointments.stream()
            .filter(a -> a.getAppointmentDateTime() != null)
            .collect(Collectors.groupingBy(
                a -> a.getAppointmentDateTime().toLocalDate().toString(),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        appointmentData.setAppointmentsByDate(byDate);
        
        // Calculate rates
        if (allAppointments.size() > 0) {
            appointmentData.setCompletionRate((double) appointmentData.getCompletedAppointments() / allAppointments.size() * 100);
            appointmentData.setCancellationRate((double) appointmentData.getCancelledAppointments() / allAppointments.size() * 100);
            appointmentData.setNoShowRate((double) appointmentData.getNoShowAppointments() / allAppointments.size() * 100);
        }
        
        reportData.setAppointmentData(appointmentData);
        report.setReportData(reportData);
        
        return analyticsReportRepository.save(report);
    }
    
    /**
     * Generate a comprehensive system overview report
     */
    public AnalyticsReport generateSystemOverviewReport(String generatedBy) {
        AnalyticsReport report = new AnalyticsReport();
        report.setReportId(generateReportId("SYS"));
        report.setReportType(AnalyticsReport.ReportType.SYSTEM_OVERVIEW);
        report.setGeneratedAt(LocalDateTime.now());
        report.setGeneratedBy(generatedBy);
        
        AnalyticsReport.ReportData reportData = new AnalyticsReport.ReportData();
        AnalyticsReport.SystemOverviewData systemData = new AnalyticsReport.SystemOverviewData();
        
        // Count all users
        int adminCount = (int) userRepository.findAll().stream()
            .filter(u -> u.getRole() == UserRole.ADMIN)
            .count();
        int staffCount = (int) userRepository.findAll().stream()
            .filter(u -> u.getRole() == UserRole.STAFF)
            .count();
        int patientCount = (int) patientRepository.count();
        int doctorCount = (int) doctorRepository.count();
        
        systemData.setTotalAdmins(adminCount);
        systemData.setTotalStaff(staffCount);
        systemData.setTotalPatients(patientCount);
        systemData.setTotalDoctors(doctorCount);
        systemData.setTotalUsers(adminCount + staffCount + patientCount + doctorCount);
        
        // Other counts
        systemData.setTotalHospitals((int) hospitalRepository.count());
        systemData.setTotalAppointments((int) appointmentRepository.count());
        systemData.setTotalMedicalRecords((int) medicalRecordRepository.count());
        
        // Users by role
        Map<String, Integer> usersByRole = new HashMap<>();
        usersByRole.put("ADMIN", adminCount);
        usersByRole.put("STAFF", staffCount);
        usersByRole.put("PATIENT", patientCount);
        usersByRole.put("DOCTOR", doctorCount);
        systemData.setUsersByRole(usersByRole);
        
        // Active vs Inactive
        long activePatients = patientRepository.findAll().stream()
            .filter(Patient::isActive)
            .count();
        Map<String, Integer> activeVsInactive = new HashMap<>();
        activeVsInactive.put("Active Patients", (int) activePatients);
        activeVsInactive.put("Inactive Patients", patientCount - (int) activePatients);
        systemData.setActiveVsInactiveUsers(activeVsInactive);
        
        reportData.setSystemData(systemData);
        report.setReportData(reportData);
        
        return analyticsReportRepository.save(report);
    }
    
    // Helper methods
    private String generateReportId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String getAgeGroup(Patient patient) {
        if (patient.getDateOfBirth() == null) {
            return "Unknown";
        }
        
        int age = Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();
        
        if (age < 18) return "0-17";
        else if (age < 30) return "18-29";
        else if (age < 45) return "30-44";
        else if (age < 60) return "45-59";
        else return "60+";
    }
    
    private AnalyticsReport.PatientDetail createPatientDetail(Patient patient) {
        AnalyticsReport.PatientDetail detail = new AnalyticsReport.PatientDetail();
        detail.setPatientId(patient.getId());  // Use MongoDB ObjectId
        detail.setPatientName(patient.getName());
        detail.setTotalAppointments(patient.getAppointments() != null ? patient.getAppointments().size() : 0);
        detail.setTotalMedicalRecords(patient.getMedicalRecords() != null ? patient.getMedicalRecords().size() : 0);
        detail.setTotalVisits(detail.getTotalAppointments() + detail.getTotalMedicalRecords());
        return detail;
    }
    
    private AnalyticsReport.DoctorPerformance createDoctorPerformance(Doctor doctor) {
        AnalyticsReport.DoctorPerformance performance = new AnalyticsReport.DoctorPerformance();
        performance.setDoctorId(doctor.getId());  // Use MongoDB ObjectId
        performance.setDoctorName(doctor.getName());
        performance.setSpecialization(doctor.getSpecialization());
        
        List<Appointment> appointments = doctor.getAppointments();
        int total = appointments != null ? appointments.size() : 0;
        performance.setTotalAppointments(total);
        
        if (appointments != null) {
            long completed = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .count();
            long cancelled = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                .count();
            
            performance.setCompletedAppointments((int) completed);
            performance.setCancelledAppointments((int) cancelled);
            performance.setCompletionRate(total > 0 ? (double) completed / total * 100 : 0.0);
        }
        
        performance.setTotalMedicalRecords(doctor.getMedicalRecords() != null ? doctor.getMedicalRecords().size() : 0);
        
        return performance;
    }
    
    private AnalyticsReport.HospitalMetrics createHospitalMetrics(Hospital hospital) {
        AnalyticsReport.HospitalMetrics metrics = new AnalyticsReport.HospitalMetrics();
        metrics.setHospitalId(hospital.getId());  // Use MongoDB ObjectId
        metrics.setHospitalName(hospital.getName());
        metrics.setType(hospital.getType() != null ? hospital.getType().toString() : "Unknown");
        metrics.setTotalDoctors(hospital.getDoctors() != null ? hospital.getDoctors().size() : 0);
        metrics.setTotalPatients(hospital.getPatients() != null ? hospital.getPatients().size() : 0);
    // Include hospitalCharges (may be null)
    metrics.setHospitalCharges(hospital.getHospitalCharges() != null ? hospital.getHospitalCharges() : java.math.BigDecimal.ZERO);
        
        // Calculate total appointments from all doctors
        int totalAppointments = 0;
        Map<String, Integer> doctorsBySpec = new HashMap<>();
        
        if (hospital.getDoctors() != null) {
            for (Doctor doctor : hospital.getDoctors()) {
                if (doctor.getAppointments() != null) {
                    totalAppointments += doctor.getAppointments().size();
                }
                String spec = doctor.getSpecialization() != null ? doctor.getSpecialization() : "Not Specified";
                doctorsBySpec.put(spec, doctorsBySpec.getOrDefault(spec, 0) + 1);
            }
        }
        
        metrics.setTotalAppointments(totalAppointments);
        metrics.setDoctorsBySpecialization(doctorsBySpec);
        
        return metrics;
    }
    
    // Retrieve reports
    public List<AnalyticsReport> getAllReports() {
        return analyticsReportRepository.findAllByOrderByGeneratedAtDesc();
    }
    
    public Optional<AnalyticsReport> getReportById(String reportId) {
        return analyticsReportRepository.findByReportId(reportId);
    }
    
    public List<AnalyticsReport> getReportsByType(AnalyticsReport.ReportType reportType) {
        return analyticsReportRepository.findByReportType(reportType);
    }
    
    public List<AnalyticsReport> getReportsByUser(String userId) {
        return analyticsReportRepository.findByGeneratedBy(userId);
    }
}
