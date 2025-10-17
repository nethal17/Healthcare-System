package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.repository.DoctorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff/check-in")
public class StaffCheckInController {

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * View today's appointments for check-in (filtered by staff's hospital)
     */
    @GetMapping("")
    public String viewCheckIns(
            @RequestParam(required = false) String filter,
            HttpSession session,
            Model model) {
        
        // Check if user is staff or admin
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null || (user.getRole() != UserRole.STAFF && user.getRole() != UserRole.ADMIN)) {
            return "redirect:/login";
        }
        
        // Get today's date
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        
        // Get all appointments for today
        List<Appointment> todaysAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDateTime() != null)
                .filter(a -> !a.getAppointmentDateTime().isBefore(startOfDay) && 
                           !a.getAppointmentDateTime().isAfter(endOfDay))
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                .sorted(Comparator.comparing(Appointment::getAppointmentDateTime))
                .collect(Collectors.toList());
        
        // Filter by hospital if staff member has a hospitalId (Admins see all)
        if (user.getRole() == UserRole.STAFF && user.getHospitalId() != null && !user.getHospitalId().isEmpty()) {
            todaysAppointments = todaysAppointments.stream()
                    .filter(appointment -> {
                        // Get the doctor for this appointment
                        if (appointment.getDoctorId() != null) {
                            return doctorRepository.findById(appointment.getDoctorId())
                                    .map(doctor -> user.getHospitalId().equals(doctor.getHospitalId()))
                                    .orElse(false);
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        
        // Apply filter if needed
        List<Appointment> filteredAppointments;
        if ("checked-in".equals(filter)) {
            filteredAppointments = todaysAppointments.stream()
                    .filter(Appointment::isCheckedIn)
                    .collect(Collectors.toList());
        } else if ("pending".equals(filter)) {
            filteredAppointments = todaysAppointments.stream()
                    .filter(a -> !a.isCheckedIn())
                    .collect(Collectors.toList());
        } else {
            filteredAppointments = todaysAppointments;
        }
        
        // Statistics
        long totalToday = todaysAppointments.size();
        long checkedInCount = todaysAppointments.stream().filter(Appointment::isCheckedIn).count();
        long pendingCount = todaysAppointments.stream().filter(a -> !a.isCheckedIn()).count();
        
        // Add to model
        model.addAttribute("user", user);
        model.addAttribute("appointments", filteredAppointments);
        model.addAttribute("filter", filter != null ? filter : "all");
        model.addAttribute("totalToday", totalToday);
        model.addAttribute("checkedInCount", checkedInCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("today", today);
        
        return "staff/check-in";
    }

    /**
     * Check-in a patient
     */
    @PostMapping("/mark/{appointmentId}")
    public String checkInPatient(
            @PathVariable String appointmentId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Check if user is staff or admin
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null || (user.getRole() != UserRole.STAFF && user.getRole() != UserRole.ADMIN)) {
            return "redirect:/login";
        }
        
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Verify staff member has access to this appointment's hospital
            if (user.getRole() == UserRole.STAFF && user.getHospitalId() != null && !user.getHospitalId().isEmpty()) {
                Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                        .orElseThrow(() -> new RuntimeException("Doctor not found"));
                
                if (!user.getHospitalId().equals(doctor.getHospitalId())) {
                    redirectAttributes.addFlashAttribute("error", "You don't have access to this appointment!");
                    return "redirect:/staff/check-in";
                }
            }
            
            // Check if already checked in
            if (appointment.isCheckedIn()) {
                redirectAttributes.addFlashAttribute("error", "Patient already checked in!");
                return "redirect:/staff/check-in";
            }
            
            // Mark as checked in
            appointment.setCheckedIn(true);
            appointment.setCheckInTime(LocalDateTime.now());
            appointment.setCheckInStaffId(user.getId());
            appointment.setCheckInStaffName(user.getName());
            appointment.setUpdatedAt(LocalDateTime.now());
            
            appointmentRepository.save(appointment);
            
            redirectAttributes.addFlashAttribute("success", 
                    "Patient " + appointment.getPatientName() + " checked in successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Failed to check in patient: " + e.getMessage());
        }
        
        return "redirect:/staff/check-in";
    }

    /**
     * Undo check-in
     */
    @PostMapping("/undo/{appointmentId}")
    public String undoCheckIn(
            @PathVariable String appointmentId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Check if user is staff or admin
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null || (user.getRole() != UserRole.STAFF && user.getRole() != UserRole.ADMIN)) {
            return "redirect:/login";
        }
        
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Verify staff member has access to this appointment's hospital
            if (user.getRole() == UserRole.STAFF && user.getHospitalId() != null && !user.getHospitalId().isEmpty()) {
                Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                        .orElseThrow(() -> new RuntimeException("Doctor not found"));
                
                if (!user.getHospitalId().equals(doctor.getHospitalId())) {
                    redirectAttributes.addFlashAttribute("error", "You don't have access to this appointment!");
                    return "redirect:/staff/check-in";
                }
            }
            
            // Check if not checked in
            if (!appointment.isCheckedIn()) {
                redirectAttributes.addFlashAttribute("error", "Patient is not checked in!");
                return "redirect:/staff/check-in";
            }
            
            // Undo check-in
            appointment.setCheckedIn(false);
            appointment.setCheckInTime(null);
            appointment.setCheckInStaffId(null);
            appointment.setCheckInStaffName(null);
            appointment.setUpdatedAt(LocalDateTime.now());
            
            appointmentRepository.save(appointment);
            
            redirectAttributes.addFlashAttribute("success", 
                    "Check-in undone for " + appointment.getPatientName());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Failed to undo check-in: " + e.getMessage());
        }
        
        return "redirect:/staff/check-in";
    }

    /**
     * Mark appointment as No Show
     */
    @PostMapping("/no-show/{appointmentId}")
    public String markNoShow(
            @PathVariable String appointmentId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Check if user is staff or admin
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null || (user.getRole() != UserRole.STAFF && user.getRole() != UserRole.ADMIN)) {
            return "redirect:/login";
        }
        
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Verify staff member has access to this appointment's hospital
            if (user.getRole() == UserRole.STAFF && user.getHospitalId() != null && !user.getHospitalId().isEmpty()) {
                Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                        .orElseThrow(() -> new RuntimeException("Doctor not found"));
                
                if (!user.getHospitalId().equals(doctor.getHospitalId())) {
                    redirectAttributes.addFlashAttribute("error", "You don't have access to this appointment!");
                    return "redirect:/staff/check-in";
                }
            }
            
            // Mark as no show
            appointment.setStatus(Appointment.AppointmentStatus.NO_SHOW);
            appointment.setUpdatedAt(LocalDateTime.now());
            
            appointmentRepository.save(appointment);
            
            redirectAttributes.addFlashAttribute("success", 
                    "Appointment marked as No Show for " + appointment.getPatientName());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Failed to mark as No Show: " + e.getMessage());
        }
        
        return "redirect:/staff/check-in";
    }
}
