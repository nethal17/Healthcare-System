package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.MedicalRecord;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.service.MedicalRecordService;
import com.example.health_care_system.service.QRCodeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/medical-records")
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordController {
    
    private final MedicalRecordService medicalRecordService;
    private final PatientRepository patientRepository;
    private final QRCodeService qrCodeService;
    
    /**
     * View medical records - accessible by patients and doctors
     * Patients can view their own records
     * Doctors can view all records they have created
     */
    @GetMapping
    public String viewMedicalRecords(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            log.warn("Unauthorized access attempt to medical records");
            redirectAttributes.addFlashAttribute("error", "Please login to view medical records");
            return "redirect:/login";
        }
        
        log.info("User {} (role: {}) accessing medical records", user.getName(), user.getRole());
        
        List<MedicalRecord> medicalRecords;
        
        // Patients can view their own medical records
        if (user.getRole() == UserRole.PATIENT) {
            medicalRecords = medicalRecordService.getPatientMedicalRecords(user.getId());
            log.info("Retrieved {} medical records for patient: {}", medicalRecords.size(), user.getName());
            model.addAttribute("viewType", "patient");
        } 
        // Doctors can view all medical records they have created
        else if (user.getRole() == UserRole.DOCTOR) {
            medicalRecords = medicalRecordService.getDoctorMedicalRecords(user.getId());
            log.info("Retrieved {} medical records for doctor: {}", medicalRecords.size(), user.getName());
            model.addAttribute("viewType", "doctor");
        } 
        // Admins can view all medical records
        else if (user.getRole() == UserRole.ADMIN) {
            medicalRecords = medicalRecordService.getAllMedicalRecords();
            log.info("Retrieved {} medical records for admin", medicalRecords.size());
            model.addAttribute("viewType", "admin");
        } 
        else {
            log.warn("Unauthorized role {} attempting to access medical records", user.getRole());
            redirectAttributes.addFlashAttribute("error", "You do not have permission to view medical records");
            return "redirect:/dashboard";
        }
        
        model.addAttribute("medicalRecords", medicalRecords);
        model.addAttribute("user", user);
        
        return "medical-records";
    }
    
    /**
     * Display QR scanner page for doctors
     */
    @GetMapping("/doctor/qr-scanner")
    public String showQRScanner(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            log.warn("Unauthorized access attempt to QR scanner");
            redirectAttributes.addFlashAttribute("error", "Please login to access the QR scanner");
            return "redirect:/login";
        }
        
        if (user.getRole() != UserRole.DOCTOR) {
            log.warn("Non-doctor user {} attempting to access QR scanner", user.getName());
            redirectAttributes.addFlashAttribute("error", "Only doctors can access the QR scanner");
            return "redirect:/dashboard";
        }
        
        model.addAttribute("user", user);
        return "doctor/qr-scanner";
    }
    
    /**
     * Handle QR code scan - fetch patient medical records
     */
    @PostMapping("/doctor/scan-patient-qr")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> scanPatientQR(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null || user.getRole() != UserRole.DOCTOR) {
                log.warn("Unauthorized QR scan attempt");
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.status(401).body(response);
            }
            
            String qrContent = request.get("patientId");
            
            if (qrContent == null || qrContent.trim().isEmpty()) {
                log.warn("Empty QR content in scan request");
                response.put("success", false);
                response.put("message", "Invalid QR code - no content found");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("Doctor {} scanning QR code with content: {}", user.getName(), qrContent);
            
            // Extract patient ID from QR code content
            String patientId;
            try {
                patientId = qrCodeService.extractUserIdFromQRContent(qrContent);
                log.info("Extracted patient ID: {}", patientId);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid QR code format: {}", qrContent);
                response.put("success", false);
                response.put("message", "Invalid QR code format");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Fetch patient information
            Optional<Patient> patientOpt = patientRepository.findById(patientId);
            
            if (patientOpt.isEmpty()) {
                log.warn("Patient not found with ID: {}", patientId);
                response.put("success", false);
                response.put("message", "Patient not found");
                return ResponseEntity.status(404).body(response);
            }
            
            Patient patient = patientOpt.get();
            
            // Fetch medical records for this patient
            List<MedicalRecord> records = medicalRecordService.getPatientMedicalRecords(patientId);
            
            log.info("Retrieved {} medical records for patient {}", records.size(), patient.getName());
            
            response.put("success", true);
            response.put("patientName", patient.getName());
            response.put("patientId", patientId);
            response.put("records", records);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing QR scan", e);
            response.put("success", false);
            response.put("message", "Error processing QR code: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Create a new medical record for a patient
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createMedicalRecord(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null || user.getRole() != UserRole.DOCTOR) {
                log.warn("Unauthorized medical record creation attempt");
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.status(401).body(response);
            }
            
            String patientId = request.get("patientId");
            String diagnosis = request.get("diagnosis");
            String prescription = request.get("prescription");
            String notes = request.get("notes");
            
            // Validate required fields
            if (patientId == null || patientId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Patient ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (diagnosis == null || diagnosis.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Diagnosis is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (prescription == null || prescription.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Prescription is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Fetch patient information
            Optional<Patient> patientOpt = patientRepository.findById(patientId);
            
            if (patientOpt.isEmpty()) {
                log.warn("Patient not found with ID: {}", patientId);
                response.put("success", false);
                response.put("message", "Patient not found");
                return ResponseEntity.status(404).body(response);
            }
            
            Patient patient = patientOpt.get();
            
            // Create medical record
            MedicalRecord medicalRecord = new MedicalRecord();
            medicalRecord.setPatientId(patientId);
            medicalRecord.setPatientName(patient.getName());
            medicalRecord.setRecordDate(LocalDate.now());
            medicalRecord.setDiagnosis(diagnosis);
            medicalRecord.setPrescription(prescription);
            medicalRecord.setNotes(notes);
            medicalRecord.setDoctorId(user.getId());
            medicalRecord.setDoctorName(user.getName());
            
            MedicalRecord savedRecord = medicalRecordService.createMedicalRecord(medicalRecord);
            
            log.info("Doctor {} created medical record {} for patient {}", 
                     user.getName(), savedRecord.getId(), patient.getName());
            
            response.put("success", true);
            response.put("message", "Medical record created successfully");
            response.put("recordId", savedRecord.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error creating medical record", e);
            response.put("success", false);
            response.put("message", "Error creating medical record: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
