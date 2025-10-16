package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.MedicalRecord;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.service.MedicalRecordService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/medical-records")
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordController {
    
    private final MedicalRecordService medicalRecordService;
    
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
}
