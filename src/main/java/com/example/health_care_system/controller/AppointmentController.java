package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.service.AppointmentService;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.repository.HospitalRepository;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.PatientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private HospitalRepository hospitalRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    /**
     * Step 1: Show all hospitals to select from
     */
    @GetMapping("/book")
    public String showBookAppointment(Model model, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Get patient details
        Patient patient = patientRepository.findById(user.getId()).orElse(null);
        if (patient == null) {
            return "redirect:/dashboard";
        }
        
        // Get all hospitals
        List<Hospital> hospitals = hospitalRepository.findAll();
        
        model.addAttribute("hospitals", hospitals);
        model.addAttribute("patient", patient);
        model.addAttribute("step", 1);
        
        return "appointments/book";
    }
    
    /**
     * Step 2: Select hospital and show doctors
     */
    @GetMapping("/book/select-doctor")
    public String selectDoctor(@RequestParam String hospitalId, Model model, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Patient patient = patientRepository.findById(user.getId()).orElse(null);
        if (patient == null) {
            return "redirect:/dashboard";
        }
        
        // Get hospital by MongoDB ObjectId
        Hospital hospital = hospitalRepository.findById(hospitalId).orElse(null);
        if (hospital == null) {
            return "redirect:/appointments/book";
        }
        
        // Get doctors in this hospital (using MongoDB ObjectId)
        List<Doctor> doctors = doctorRepository.findByHospitalId(hospitalId);
        
        model.addAttribute("hospital", hospital);
        model.addAttribute("doctors", doctors);
        model.addAttribute("patient", patient);
        model.addAttribute("step", 2);
        
        return "appointments/select-doctor";
    }
    
    /**
     * Step 3: Select doctor and show available time slots
     */
    @GetMapping("/book/select-timeslot")
    public String selectTimeSlot(
            @RequestParam String hospitalId,
            @RequestParam String doctorId,
            @RequestParam(required = false) String date,
            Model model, 
            HttpSession session) {
        
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Patient patient = patientRepository.findById(user.getId()).orElse(null);
        if (patient == null) {
            return "redirect:/dashboard";
        }
        
        // Get hospital and doctor by MongoDB ObjectId
        Hospital hospital = hospitalRepository.findById(hospitalId).orElse(null);
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        
        if (hospital == null || doctor == null) {
            return "redirect:/appointments/book";
        }
        
        // Get selected date or default to today
        LocalDate selectedDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        
        // Get available time slots for the selected date
        List<LocalTime> availableSlots = appointmentService.getAvailableTimeSlots(doctorId, selectedDate);
        
        // Filter slots into morning (before 1 PM) and afternoon (1 PM and after)
        List<LocalTime> morningSlots = availableSlots.stream()
                .filter(slot -> slot.getHour() < 13)
                .toList();
        List<LocalTime> afternoonSlots = availableSlots.stream()
                .filter(slot -> slot.getHour() >= 13)
                .toList();
        
        // Get next 7 days for date selection
        List<LocalDate> availableDates = appointmentService.getNextSevenDays();
        
        model.addAttribute("hospital", hospital);
        model.addAttribute("doctor", doctor);
        model.addAttribute("patient", patient);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("morningSlots", morningSlots);
        model.addAttribute("afternoonSlots", afternoonSlots);
        model.addAttribute("availableDates", availableDates);
        model.addAttribute("step", 3);
        
        return "appointments/select-timeslot";
    }
    
    /**
     * Step 4: Confirm booking
     */
    @GetMapping("/book/confirm")
    public String confirmBooking(
            @RequestParam String hospitalId,
            @RequestParam String doctorId,
            @RequestParam String date,
            @RequestParam String time,
            Model model,
            HttpSession session) {
        
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Patient patient = patientRepository.findById(user.getId()).orElse(null);
        if (patient == null) {
            return "redirect:/dashboard";
        }
        
        // Get hospital and doctor by MongoDB ObjectId
        Hospital hospital = hospitalRepository.findById(hospitalId).orElse(null);
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        
        if (hospital == null || doctor == null) {
            return "redirect:/appointments/book";
        }
        
        // Parse date and time
        LocalDate selectedDate = LocalDate.parse(date);
        LocalTime selectedTime = LocalTime.parse(time);
        LocalDateTime appointmentDateTime = LocalDateTime.of(selectedDate, selectedTime);
        
        model.addAttribute("hospital", hospital);
        model.addAttribute("doctor", doctor);
        model.addAttribute("patient", patient);
        model.addAttribute("appointmentDateTime", appointmentDateTime);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedTime", selectedTime);
        model.addAttribute("step", 4);
        
        return "appointments/confirm";
    }
    
    /**
     * Process the booking
     */
    @PostMapping("/book/process")
    public String processBooking(
            @RequestParam String doctorId,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String notes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Patient patient = patientRepository.findById(user.getId()).orElse(null);
        if (patient == null) {
            redirectAttributes.addFlashAttribute("error", "Patient not found");
            return "redirect:/appointments/book";
        }
        
        try {
            // Parse date and time
            LocalDate selectedDate = LocalDate.parse(date);
            LocalTime selectedTime = LocalTime.parse(time);
            LocalDateTime appointmentDateTime = LocalDateTime.of(selectedDate, selectedTime);
            
            // Create appointment using MongoDB ObjectIds
            Appointment appointment = appointmentService.bookAppointment(
                patient.getId(),  // Use MongoDB ObjectId
                patient.getName(),
                doctorId,
                appointmentDateTime,
                purpose,
                notes
            );
            
            redirectAttributes.addFlashAttribute("success", "Appointment booked successfully!");
            redirectAttributes.addFlashAttribute("appointmentId", appointment.getId());  // Use MongoDB ObjectId
            
            return "redirect:/appointments/success";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to book appointment: " + e.getMessage());
            return "redirect:/appointments/book";
        }
    }
    
    /**
     * Show booking success page
     */
    @GetMapping("/success")
    public String bookingSuccess(Model model, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Patient patient = patientRepository.findById(user.getId()).orElse(null);
        model.addAttribute("patient", patient);
        
        return "appointments/success";
    }
    
    /**
     * View patient's appointments
     */
    @GetMapping("/my-appointments")
    public String myAppointments(Model model, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Patient patient = patientRepository.findById(user.getId()).orElse(null);
        if (patient == null) {
            return "redirect:/dashboard";
        }
        
        // Get patient's appointments using MongoDB ObjectId
        List<Appointment> appointments = appointmentService.getPatientAppointments(patient.getId());
        
        // Calculate which appointments can be cancelled (within 6 hours of creation)
        LocalDateTime now = LocalDateTime.now();
        Map<String, Boolean> cancellableAppointments = new HashMap<>();
        Map<String, Long> hoursRemaining = new HashMap<>();
        
        for (Appointment appointment : appointments) {
            if (appointment.getCreatedAt() != null) {
                long hoursSinceCreation = java.time.Duration.between(appointment.getCreatedAt(), now).toHours();
                boolean canCancel = hoursSinceCreation < 6;
                cancellableAppointments.put(appointment.getId(), canCancel);
                
                if (canCancel) {
                    hoursRemaining.put(appointment.getId(), 6 - hoursSinceCreation);
                }
            } else {
                // If createdAt is null, allow cancellation by default
                cancellableAppointments.put(appointment.getId(), true);
            }
        }
        
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        model.addAttribute("cancellableAppointments", cancellableAppointments);
        model.addAttribute("hoursRemaining", hoursRemaining);
        
        return "appointments/my-appointments";
    }
    
    /**
     * Cancel appointment
     */
    @PostMapping("/cancel/{appointmentId}")
    @ResponseBody
    public Map<String, Object> cancelAppointment(@PathVariable String appointmentId, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "User not logged in");
        }
        
        try {
            // Get the appointment to check the creation time
            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
            if (appointment == null) {
                return Map.of("success", false, "message", "Appointment not found");
            }
            
            // Check if appointment was created more than 6 hours ago
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime createdAt = appointment.getCreatedAt();
            long hoursSinceCreation = java.time.Duration.between(createdAt, now).toHours();
            
            if (hoursSinceCreation >= 6) {
                // Get doctor and hospital information
                Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
                if (doctor != null) {
                    Hospital hospital = hospitalRepository.findById(doctor.getHospitalId()).orElse(null);
                    if (hospital != null) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", false);
                        response.put("cannotCancel", true);
                        response.put("message", "Cancellation window expired");
                        response.put("reason", "You can only cancel appointments within 6 hours of booking. This appointment was booked " + hoursSinceCreation + " hours ago.");
                        response.put("hospitalName", hospital.getName());
                        response.put("hospitalPhone", hospital.getContactInfo() != null ? hospital.getContactInfo().getPhoneNumber() : "Not available");
                        response.put("hospitalEmail", hospital.getContactInfo() != null ? hospital.getContactInfo().getEmail() : "Not available");
                        response.put("hospitalWebsite", hospital.getContactInfo() != null ? hospital.getContactInfo().getWebsite() : "Not available");
                        return response;
                    }
                }
                return Map.of(
                    "success", false,
                    "cannotCancel", true,
                    "message", "Cancellation window expired",
                    "reason", "You can only cancel appointments within 6 hours of booking. Please contact the hospital to cancel this appointment."
                );
            }
            
            // If within 6 hours, proceed with cancellation
            appointmentService.cancelAppointment(appointmentId);
            return Map.of("success", true, "message", "Appointment cancelled successfully");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    /**
     * Get available slots for a date (AJAX endpoint)
     */
    @GetMapping("/available-slots")
    @ResponseBody
    public List<LocalTime> getAvailableSlots(
            @RequestParam String doctorId,
            @RequestParam String date) {
        
        LocalDate selectedDate = LocalDate.parse(date);
        return appointmentService.getAvailableTimeSlots(doctorId, selectedDate);
    }
}
