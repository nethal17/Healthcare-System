package com.example.health_care_system.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "analytics_reports")
public class AnalyticsReport {
    
    @Id
    private String id;
    private String reportId;
    private ReportType reportType;
    private LocalDateTime generatedAt;
    private LocalDateTime reportPeriodStart;
    private LocalDateTime reportPeriodEnd;
    private String generatedBy; // User ID who generated the report
    private ReportData reportData;
    
    // Enums
    public enum ReportType {
        PATIENT_SUMMARY,
        DOCTOR_PERFORMANCE,
        HOSPITAL_OVERVIEW,
        APPOINTMENT_STATISTICS,
        MEDICAL_RECORDS_SUMMARY,
        SYSTEM_OVERVIEW
    }
    
    // Nested classes for different report data structures
    public static class ReportData {
        private PatientReportData patientData;
        private DoctorReportData doctorData;
        private HospitalReportData hospitalData;
        private AppointmentReportData appointmentData;
        private SystemOverviewData systemData;
        
        // Getters and Setters
        public PatientReportData getPatientData() { return patientData; }
        public void setPatientData(PatientReportData patientData) { this.patientData = patientData; }
        
        public DoctorReportData getDoctorData() { return doctorData; }
        public void setDoctorData(DoctorReportData doctorData) { this.doctorData = doctorData; }
        
        public HospitalReportData getHospitalData() { return hospitalData; }
        public void setHospitalData(HospitalReportData hospitalData) { this.hospitalData = hospitalData; }
        
        public AppointmentReportData getAppointmentData() { return appointmentData; }
        public void setAppointmentData(AppointmentReportData appointmentData) { this.appointmentData = appointmentData; }
        
        public SystemOverviewData getSystemData() { return systemData; }
        public void setSystemData(SystemOverviewData systemData) { this.systemData = systemData; }
    }
    
    // Patient Report Data
    public static class PatientReportData {
        private int totalPatients;
        private int activePatients;
        private int inactivePatients;
        private Map<String, Integer> patientsByGender;
        private Map<String, Integer> patientsByAgeGroup;
        private Map<String, Integer> patientsByHospital;
        private int patientsWithAppointments;
        private int patientsWithMedicalRecords;
        private List<PatientDetail> topPatientsByVisits;
        
        // Getters and Setters
        public int getTotalPatients() { return totalPatients; }
        public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }
        
        public int getActivePatients() { return activePatients; }
        public void setActivePatients(int activePatients) { this.activePatients = activePatients; }
        
        public int getInactivePatients() { return inactivePatients; }
        public void setInactivePatients(int inactivePatients) { this.inactivePatients = inactivePatients; }
        
        public Map<String, Integer> getPatientsByGender() { return patientsByGender; }
        public void setPatientsByGender(Map<String, Integer> patientsByGender) { this.patientsByGender = patientsByGender; }
        
        public Map<String, Integer> getPatientsByAgeGroup() { return patientsByAgeGroup; }
        public void setPatientsByAgeGroup(Map<String, Integer> patientsByAgeGroup) { this.patientsByAgeGroup = patientsByAgeGroup; }
        
        public Map<String, Integer> getPatientsByHospital() { return patientsByHospital; }
        public void setPatientsByHospital(Map<String, Integer> patientsByHospital) { this.patientsByHospital = patientsByHospital; }
        
        public int getPatientsWithAppointments() { return patientsWithAppointments; }
        public void setPatientsWithAppointments(int patientsWithAppointments) { this.patientsWithAppointments = patientsWithAppointments; }
        
        public int getPatientsWithMedicalRecords() { return patientsWithMedicalRecords; }
        public void setPatientsWithMedicalRecords(int patientsWithMedicalRecords) { this.patientsWithMedicalRecords = patientsWithMedicalRecords; }
        
        public List<PatientDetail> getTopPatientsByVisits() { return topPatientsByVisits; }
        public void setTopPatientsByVisits(List<PatientDetail> topPatientsByVisits) { this.topPatientsByVisits = topPatientsByVisits; }
    }
    
    // Doctor Report Data
    public static class DoctorReportData {
        private int totalDoctors;
        private int activeDoctors;
        private Map<String, Integer> doctorsBySpecialization;
        private Map<String, Integer> doctorsByHospital;
        private Map<String, Integer> doctorsByGender;
        private List<DoctorPerformance> topDoctorsByAppointments;
        private double averageAppointmentsPerDoctor;
        private int doctorsWithMedicalRecords;
        
        // Getters and Setters
        public int getTotalDoctors() { return totalDoctors; }
        public void setTotalDoctors(int totalDoctors) { this.totalDoctors = totalDoctors; }
        
        public int getActiveDoctors() { return activeDoctors; }
        public void setActiveDoctors(int activeDoctors) { this.activeDoctors = activeDoctors; }
        
        public Map<String, Integer> getDoctorsBySpecialization() { return doctorsBySpecialization; }
        public void setDoctorsBySpecialization(Map<String, Integer> doctorsBySpecialization) { this.doctorsBySpecialization = doctorsBySpecialization; }
        
        public Map<String, Integer> getDoctorsByHospital() { return doctorsByHospital; }
        public void setDoctorsByHospital(Map<String, Integer> doctorsByHospital) { this.doctorsByHospital = doctorsByHospital; }
        
        public Map<String, Integer> getDoctorsByGender() { return doctorsByGender; }
        public void setDoctorsByGender(Map<String, Integer> doctorsByGender) { this.doctorsByGender = doctorsByGender; }
        
        public List<DoctorPerformance> getTopDoctorsByAppointments() { return topDoctorsByAppointments; }
        public void setTopDoctorsByAppointments(List<DoctorPerformance> topDoctorsByAppointments) { this.topDoctorsByAppointments = topDoctorsByAppointments; }
        
        public double getAverageAppointmentsPerDoctor() { return averageAppointmentsPerDoctor; }
        public void setAverageAppointmentsPerDoctor(double averageAppointmentsPerDoctor) { this.averageAppointmentsPerDoctor = averageAppointmentsPerDoctor; }
        
        public int getDoctorsWithMedicalRecords() { return doctorsWithMedicalRecords; }
        public void setDoctorsWithMedicalRecords(int doctorsWithMedicalRecords) { this.doctorsWithMedicalRecords = doctorsWithMedicalRecords; }
    }
    
    // Hospital Report Data
    public static class HospitalReportData {
        private int totalHospitals;
        private int governmentHospitals;
        private int privateHospitals;
        private Map<String, Integer> hospitalsByLocation;
        private Map<String, HospitalMetrics> hospitalMetrics;
        private int totalDoctorsAcrossHospitals;
        private int totalPatientsAcrossHospitals;
        private double averageDoctorsPerHospital;
        private double averagePatientsPerHospital;
        
        // Getters and Setters
        public int getTotalHospitals() { return totalHospitals; }
        public void setTotalHospitals(int totalHospitals) { this.totalHospitals = totalHospitals; }
        
        public int getGovernmentHospitals() { return governmentHospitals; }
        public void setGovernmentHospitals(int governmentHospitals) { this.governmentHospitals = governmentHospitals; }
        
        public int getPrivateHospitals() { return privateHospitals; }
        public void setPrivateHospitals(int privateHospitals) { this.privateHospitals = privateHospitals; }
        
        public Map<String, Integer> getHospitalsByLocation() { return hospitalsByLocation; }
        public void setHospitalsByLocation(Map<String, Integer> hospitalsByLocation) { this.hospitalsByLocation = hospitalsByLocation; }
        
        public Map<String, HospitalMetrics> getHospitalMetrics() { return hospitalMetrics; }
        public void setHospitalMetrics(Map<String, HospitalMetrics> hospitalMetrics) { this.hospitalMetrics = hospitalMetrics; }
        
        public int getTotalDoctorsAcrossHospitals() { return totalDoctorsAcrossHospitals; }
        public void setTotalDoctorsAcrossHospitals(int totalDoctorsAcrossHospitals) { this.totalDoctorsAcrossHospitals = totalDoctorsAcrossHospitals; }
        
        public int getTotalPatientsAcrossHospitals() { return totalPatientsAcrossHospitals; }
        public void setTotalPatientsAcrossHospitals(int totalPatientsAcrossHospitals) { this.totalPatientsAcrossHospitals = totalPatientsAcrossHospitals; }
        
        public double getAverageDoctorsPerHospital() { return averageDoctorsPerHospital; }
        public void setAverageDoctorsPerHospital(double averageDoctorsPerHospital) { this.averageDoctorsPerHospital = averageDoctorsPerHospital; }
        
        public double getAveragePatientsPerHospital() { return averagePatientsPerHospital; }
        public void setAveragePatientsPerHospital(double averagePatientsPerHospital) { this.averagePatientsPerHospital = averagePatientsPerHospital; }
    }
    
    // Appointment Report Data
    public static class AppointmentReportData {
        private int totalAppointments;
        private int scheduledAppointments;
        private int completedAppointments;
        private int cancelledAppointments;
        private int noShowAppointments;
        private Map<String, Integer> appointmentsByDoctor;
        private Map<String, Integer> appointmentsBySpecialization;
        private Map<String, Integer> appointmentsByHospital;
        private Map<String, Integer> appointmentsByDate;
        private double completionRate;
        private double cancellationRate;
        private double noShowRate;
        
        // Getters and Setters
        public int getTotalAppointments() { return totalAppointments; }
        public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }
        
        public int getScheduledAppointments() { return scheduledAppointments; }
        public void setScheduledAppointments(int scheduledAppointments) { this.scheduledAppointments = scheduledAppointments; }
        
        public int getCompletedAppointments() { return completedAppointments; }
        public void setCompletedAppointments(int completedAppointments) { this.completedAppointments = completedAppointments; }
        
        public int getCancelledAppointments() { return cancelledAppointments; }
        public void setCancelledAppointments(int cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }
        
        public int getNoShowAppointments() { return noShowAppointments; }
        public void setNoShowAppointments(int noShowAppointments) { this.noShowAppointments = noShowAppointments; }
        
        public Map<String, Integer> getAppointmentsByDoctor() { return appointmentsByDoctor; }
        public void setAppointmentsByDoctor(Map<String, Integer> appointmentsByDoctor) { this.appointmentsByDoctor = appointmentsByDoctor; }
        
        public Map<String, Integer> getAppointmentsBySpecialization() { return appointmentsBySpecialization; }
        public void setAppointmentsBySpecialization(Map<String, Integer> appointmentsBySpecialization) { this.appointmentsBySpecialization = appointmentsBySpecialization; }
        
        public Map<String, Integer> getAppointmentsByHospital() { return appointmentsByHospital; }
        public void setAppointmentsByHospital(Map<String, Integer> appointmentsByHospital) { this.appointmentsByHospital = appointmentsByHospital; }
        
        public Map<String, Integer> getAppointmentsByDate() { return appointmentsByDate; }
        public void setAppointmentsByDate(Map<String, Integer> appointmentsByDate) { this.appointmentsByDate = appointmentsByDate; }
        
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
        
        public double getCancellationRate() { return cancellationRate; }
        public void setCancellationRate(double cancellationRate) { this.cancellationRate = cancellationRate; }
        
        public double getNoShowRate() { return noShowRate; }
        public void setNoShowRate(double noShowRate) { this.noShowRate = noShowRate; }
    }
    
    // System Overview Data
    public static class SystemOverviewData {
        private int totalUsers;
        private int totalPatients;
        private int totalDoctors;
        private int totalStaff;
        private int totalAdmins;
        private int totalHospitals;
        private int totalAppointments;
        private int totalMedicalRecords;
        private Map<String, Integer> usersByRole;
        private Map<String, Integer> activeVsInactiveUsers;
        
        // Getters and Setters
        public int getTotalUsers() { return totalUsers; }
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
        
        public int getTotalPatients() { return totalPatients; }
        public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }
        
        public int getTotalDoctors() { return totalDoctors; }
        public void setTotalDoctors(int totalDoctors) { this.totalDoctors = totalDoctors; }
        
        public int getTotalStaff() { return totalStaff; }
        public void setTotalStaff(int totalStaff) { this.totalStaff = totalStaff; }
        
        public int getTotalAdmins() { return totalAdmins; }
        public void setTotalAdmins(int totalAdmins) { this.totalAdmins = totalAdmins; }
        
        public int getTotalHospitals() { return totalHospitals; }
        public void setTotalHospitals(int totalHospitals) { this.totalHospitals = totalHospitals; }
        
        public int getTotalAppointments() { return totalAppointments; }
        public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }
        
        public int getTotalMedicalRecords() { return totalMedicalRecords; }
        public void setTotalMedicalRecords(int totalMedicalRecords) { this.totalMedicalRecords = totalMedicalRecords; }
        
        public Map<String, Integer> getUsersByRole() { return usersByRole; }
        public void setUsersByRole(Map<String, Integer> usersByRole) { this.usersByRole = usersByRole; }
        
        public Map<String, Integer> getActiveVsInactiveUsers() { return activeVsInactiveUsers; }
        public void setActiveVsInactiveUsers(Map<String, Integer> activeVsInactiveUsers) { this.activeVsInactiveUsers = activeVsInactiveUsers; }
    }
    
    // Supporting classes
    public static class PatientDetail {
        private String patientId;
        private String patientName;
        private int totalVisits;
        private int totalAppointments;
        private int totalMedicalRecords;
        
        // Getters and Setters
        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }
        
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        
        public int getTotalVisits() { return totalVisits; }
        public void setTotalVisits(int totalVisits) { this.totalVisits = totalVisits; }
        
        public int getTotalAppointments() { return totalAppointments; }
        public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }
        
        public int getTotalMedicalRecords() { return totalMedicalRecords; }
        public void setTotalMedicalRecords(int totalMedicalRecords) { this.totalMedicalRecords = totalMedicalRecords; }
    }
    
    public static class DoctorPerformance {
        private String doctorId;
        private String doctorName;
        private String specialization;
        private int totalAppointments;
        private int completedAppointments;
        private int cancelledAppointments;
        private int totalMedicalRecords;
        private double completionRate;
        
        // Getters and Setters
        public String getDoctorId() { return doctorId; }
        public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
        
        public String getDoctorName() { return doctorName; }
        public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
        
        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }
        
        public int getTotalAppointments() { return totalAppointments; }
        public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }
        
        public int getCompletedAppointments() { return completedAppointments; }
        public void setCompletedAppointments(int completedAppointments) { this.completedAppointments = completedAppointments; }
        
        public int getCancelledAppointments() { return cancelledAppointments; }
        public void setCancelledAppointments(int cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }
        
        public int getTotalMedicalRecords() { return totalMedicalRecords; }
        public void setTotalMedicalRecords(int totalMedicalRecords) { this.totalMedicalRecords = totalMedicalRecords; }
        
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    }
    
    public static class HospitalMetrics {
        private String hospitalId;
        private String hospitalName;
    private java.math.BigDecimal hospitalCharges;
        private String type;
        private int totalDoctors;
        private int totalPatients;
        private int totalAppointments;
        private Map<String, Integer> doctorsBySpecialization;
        
        // Getters and Setters
        public String getHospitalId() { return hospitalId; }
        public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
        
        public String getHospitalName() { return hospitalName; }
        public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public java.math.BigDecimal getHospitalCharges() { return hospitalCharges; }
    public void setHospitalCharges(java.math.BigDecimal hospitalCharges) { this.hospitalCharges = hospitalCharges; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public int getTotalDoctors() { return totalDoctors; }
        public void setTotalDoctors(int totalDoctors) { this.totalDoctors = totalDoctors; }
        
        public int getTotalPatients() { return totalPatients; }
        public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }
        
        public int getTotalAppointments() { return totalAppointments; }
        public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }
        
        public Map<String, Integer> getDoctorsBySpecialization() { return doctorsBySpecialization; }
        public void setDoctorsBySpecialization(Map<String, Integer> doctorsBySpecialization) { this.doctorsBySpecialization = doctorsBySpecialization; }
    }
    
    // Main class getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    
    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public LocalDateTime getReportPeriodStart() { return reportPeriodStart; }
    public void setReportPeriodStart(LocalDateTime reportPeriodStart) { this.reportPeriodStart = reportPeriodStart; }
    
    public LocalDateTime getReportPeriodEnd() { return reportPeriodEnd; }
    public void setReportPeriodEnd(LocalDateTime reportPeriodEnd) { this.reportPeriodEnd = reportPeriodEnd; }
    
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    
    public ReportData getReportData() { return reportData; }
    public void setReportData(ReportData reportData) { this.reportData = reportData; }
}
