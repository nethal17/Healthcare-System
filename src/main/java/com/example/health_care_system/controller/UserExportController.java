package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.User;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserExportController {
    
    private final UserService userService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    
    @GetMapping("/users/export")
    public void exportUsers(
            @RequestParam(required = false) String role,
            HttpSession session,
            HttpServletResponse response) throws IOException {
        
        // Check if user is logged in and is an admin
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        // Set response headers for CSV download
        response.setContentType("text/csv");
        String filename = "users_export_" + System.currentTimeMillis() + ".csv";
        if (role != null && !role.equals("ALL")) {
            filename = role.toLowerCase() + "s_export_" + System.currentTimeMillis() + ".csv";
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        PrintWriter writer = response.getWriter();
        
        // Export based on filter
        if (role == null || role.equals("ALL")) {
            exportAllUsers(writer);
        } else if (role.equals("PATIENT")) {
            exportPatients(writer);
        } else if (role.equals("DOCTOR")) {
            exportDoctors(writer);
        } else if (role.equals("STAFF")) {
            exportStaff(writer);
        }
        
        writer.flush();
    }
    
    private void exportAllUsers(PrintWriter writer) {
        // Write headers for all users export
        writer.println("User Type,Name,Email,Gender,Contact Number,Specialization/DOB,Address,Joined Date,Status");
        
        // Export Patients
        List<Patient> patients = userService.getAllPatients();
        for (Patient patient : patients) {
            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                "Patient",
                escapeCsv(patient.getName()),
                escapeCsv(patient.getEmail()),
                escapeCsv(patient.getGender()),
                escapeCsv(patient.getContactNumber()),
                patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(DATE_FORMATTER) : "",
                escapeCsv(patient.getAddress()),
                patient.getCreatedAt() != null ? patient.getCreatedAt().format(DATETIME_FORMATTER) : "",
                patient.isActive() ? "Active" : "Inactive"
            ));
        }
        
        // Export Doctors
        List<Doctor> doctors = userService.getAllDoctors();
        for (Doctor doctor : doctors) {
            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                "Doctor",
                escapeCsv(doctor.getName()),
                escapeCsv(doctor.getEmail()),
                escapeCsv(doctor.getGender()),
                escapeCsv(doctor.getContactNumber()),
                escapeCsv(doctor.getSpecialization()),
                "",
                doctor.getCreatedAt() != null ? doctor.getCreatedAt().format(DATETIME_FORMATTER) : "",
                "Active"
            ));
        }
        
        // Export Staff
        List<User> staff = userService.getUsersByRole(UserRole.STAFF);
        for (User staffMember : staff) {
            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                "Staff",
                escapeCsv(staffMember.getName()),
                escapeCsv(staffMember.getEmail()),
                escapeCsv(staffMember.getGender()),
                escapeCsv(staffMember.getContactNumber()),
                "",
                "",
                staffMember.getCreatedAt() != null ? staffMember.getCreatedAt().format(DATETIME_FORMATTER) : "",
                "Active"
            ));
        }
    }
    
    private void exportPatients(PrintWriter writer) {
        // Write headers for patients
        writer.println("Name,Email,Gender,Contact Number,Date of Birth,Address,Joined Date,Status");
        
        List<Patient> patients = userService.getAllPatients();
        for (Patient patient : patients) {
            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                escapeCsv(patient.getName()),
                escapeCsv(patient.getEmail()),
                escapeCsv(patient.getGender()),
                escapeCsv(patient.getContactNumber()),
                patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(DATE_FORMATTER) : "",
                escapeCsv(patient.getAddress()),
                patient.getCreatedAt() != null ? patient.getCreatedAt().format(DATETIME_FORMATTER) : "",
                patient.isActive() ? "Active" : "Inactive"
            ));
        }
    }
    
    private void exportDoctors(PrintWriter writer) {
        // Write headers for doctors
        writer.println("Name,Email,Gender,Contact Number,Specialization,Joined Date");
        
        List<Doctor> doctors = userService.getAllDoctors();
        for (Doctor doctor : doctors) {
            writer.println(String.format("%s,%s,%s,%s,%s,%s",
                escapeCsv(doctor.getName()),
                escapeCsv(doctor.getEmail()),
                escapeCsv(doctor.getGender()),
                escapeCsv(doctor.getContactNumber()),
                escapeCsv(doctor.getSpecialization()),
                doctor.getCreatedAt() != null ? doctor.getCreatedAt().format(DATETIME_FORMATTER) : ""
            ));
        }
    }
    
    private void exportStaff(PrintWriter writer) {
        // Write headers for staff
        writer.println("Name,Email,Gender,Contact Number,Joined Date");
        
        List<User> staff = userService.getUsersByRole(UserRole.STAFF);
        for (User staffMember : staff) {
            writer.println(String.format("%s,%s,%s,%s,%s",
                escapeCsv(staffMember.getName()),
                escapeCsv(staffMember.getEmail()),
                escapeCsv(staffMember.getGender()),
                escapeCsv(staffMember.getContactNumber()),
                staffMember.getCreatedAt() != null ? staffMember.getCreatedAt().format(DATETIME_FORMATTER) : ""
            ));
        }
    }
    
    /**
     * Escape CSV special characters
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // If the value contains comma, quote, or newline, wrap it in quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // Escape existing quotes by doubling them
            value = value.replace("\"", "\"\"");
            // Wrap in quotes
            return "\"" + value + "\"";
        }
        
        return value;
    }
}
