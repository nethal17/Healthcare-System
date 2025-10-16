package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Payment;
import com.example.health_care_system.model.TimeSlotReservation;
import com.example.health_care_system.service.AppointmentService;
import com.example.health_care_system.service.PaymentService;
import com.example.health_care_system.service.PdfGenerationService;
import com.example.health_care_system.service.TimeSlotReservationService;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.repository.HospitalRepository;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.PatientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PdfGenerationService pdfGenerationService;
    
    @Autowired
    private TimeSlotReservationService reservationService;
    
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
        
        // Get available time slots for the selected date (excluding current patient's reservations)
        List<LocalTime> availableSlots = appointmentService.getAvailableTimeSlots(doctorId, selectedDate, patient.getId());
        
        // Get reserved time slots (by other users)
        List<LocalTime> reservedSlots = appointmentService.getReservedTimeSlots(doctorId, selectedDate, patient.getId());
        
        // Filter slots into morning (before 1 PM) and afternoon (1 PM and after)
        List<LocalTime> morningSlots = availableSlots.stream()
                .filter(slot -> slot.getHour() < 13)
                .toList();
        List<LocalTime> afternoonSlots = availableSlots.stream()
                .filter(slot -> slot.getHour() >= 13)
                .toList();
        
        // Filter reserved slots into morning and afternoon
        List<LocalTime> morningReservedSlots = reservedSlots.stream()
                .filter(slot -> slot.getHour() < 13)
                .toList();
        List<LocalTime> afternoonReservedSlots = reservedSlots.stream()
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
        model.addAttribute("reservedSlots", reservedSlots);
        model.addAttribute("morningReservedSlots", morningReservedSlots);
        model.addAttribute("afternoonReservedSlots", afternoonReservedSlots);
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
     * Store appointment details in session and redirect to payment selection
     */
    @PostMapping("/book/process")
    public String processBooking(
            @RequestParam String doctorId,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String hospitalType,
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
            // For GOVERNMENT hospitals, directly book the appointment without payment
            if ("GOVERNMENT".equals(hospitalType)) {
                // Verify reservation is still valid
            if (!reservationService.isReservationValid(patient.getId(), session.getId())) {
                redirectAttributes.addFlashAttribute("error", "Your reservation has expired. Please select a time slot again.");
                return "redirect:/appointments/book";
            }
            
            // Parse date and time
                LocalDate selectedDate = LocalDate.parse(date);
                LocalTime selectedTime = LocalTime.parse(time);
                LocalDateTime appointmentDateTime = LocalDateTime.of(selectedDate, selectedTime);
                
                // Create appointment directly
                Appointment appointment = appointmentService.bookAppointment(
                    patient.getId(),
                    patient.getName(),
                    doctorId,
                    appointmentDateTime,
                    purpose != null ? purpose : "",
                    notes != null ? notes : ""
                );
                
                // Redirect to success page
                redirectAttributes.addFlashAttribute("success", "Appointment booked successfully!");
                redirectAttributes.addFlashAttribute("appointmentId", appointment.getId());
                redirectAttributes.addFlashAttribute("paymentMethod", "FREE"); // Government hospitals are free
                return "redirect:/appointments/success";
            }
            
            // For PRIVATE hospitals, store appointment details in session and proceed to payment
            Map<String, String> appointmentDetails = new HashMap<>();
            appointmentDetails.put("doctorId", doctorId);
            appointmentDetails.put("date", date);
            appointmentDetails.put("time", time);
            appointmentDetails.put("purpose", purpose != null ? purpose : "");
            appointmentDetails.put("notes", notes != null ? notes : "");
            
            session.setAttribute("pendingAppointment", appointmentDetails);
            
            // Redirect to payment selection page
            return "redirect:/appointments/payment-selection?doctorId=" + doctorId;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to proceed: " + e.getMessage());
            return "redirect:/appointments/book";
        }
    }
    
    /**
     * Show payment method selection page
     */
    @GetMapping("/payment-selection")
    public String showPaymentSelection(
            @RequestParam String doctorId,
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
        
        // Get appointment details from session
        @SuppressWarnings("unchecked")
        Map<String, String> appointmentDetails = (Map<String, String>) session.getAttribute("pendingAppointment");
        if (appointmentDetails == null) {
            return "redirect:/appointments/book";
        }
        
        // Get doctor and hospital details
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return "redirect:/appointments/book";
        }
        
        Hospital hospital = hospitalRepository.findById(doctor.getHospitalId()).orElse(null);
        if (hospital == null) {
            return "redirect:/appointments/book";
        }
        
        model.addAttribute("patient", patient);
        model.addAttribute("doctor", doctor);
        model.addAttribute("hospital", hospital);
        model.addAttribute("appointmentDetails", appointmentDetails);
        
        return "appointments/payment-selection";
    }
    
    /**
     * Process payment method selection and create appointment
     */
    @PostMapping("/payment-selection/process")
    public String processPaymentSelection(
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String insuranceProvider,
            @RequestParam(required = false) String policyNumber,
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
        
        // Get appointment details from session
        @SuppressWarnings("unchecked")
        Map<String, String> appointmentDetails = (Map<String, String>) session.getAttribute("pendingAppointment");
        if (appointmentDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Appointment details not found");
            return "redirect:/appointments/book";
        }
        
        // If insurance is selected, redirect to InsuranceCollection page
        if ("INSURANCE".equals(paymentMethod)) {
            // Keep appointment details in session for later use
            session.setAttribute("selectedPaymentMethod", paymentMethod);
            return "redirect:/InsuranceCollection";
        }
        
        try {
            // Parse date and time
            LocalDate selectedDate = LocalDate.parse(appointmentDetails.get("date"));
            LocalTime selectedTime = LocalTime.parse(appointmentDetails.get("time"));
            LocalDateTime appointmentDateTime = LocalDateTime.of(selectedDate, selectedTime);
            
            // Create appointment
            Appointment appointment = appointmentService.bookAppointment(
                patient.getId(),
                patient.getName(),
                appointmentDetails.get("doctorId"),
                appointmentDateTime,
                appointmentDetails.get("purpose"),
                appointmentDetails.get("notes")
            );
            
            // Confirm the reservation (marks it as CONFIRMED)
            reservationService.confirmReservation(patient.getId(), session.getId());
            
            // Store payment method selection in session for further processing
            session.setAttribute("selectedPaymentMethod", paymentMethod);
            session.setAttribute("appointmentId", appointment.getId());
            
            // Get doctor and hospital for payment record
            Doctor doctor = doctorRepository.findById(appointmentDetails.get("doctorId")).orElse(null);
            if (doctor != null) {
                Hospital hospital = hospitalRepository.findById(doctor.getHospitalId()).orElse(null);
                if (hospital != null && hospital.getHospitalCharges() != null) {
                    // Create payment record for cash payment
                    if ("CASH".equals(paymentMethod)) {
                        Payment payment = paymentService.createCashPayment(
                            appointment.getId(),
                            hospital.getHospitalCharges()
                        );
                        session.setAttribute("paymentId", payment.getId());
                    }
                }
            }
            
            // Clear pending appointment from session
            session.removeAttribute("pendingAppointment");
            
            // Redirect based on payment method
            if ("CARD".equals(paymentMethod)) {
                // Redirect to card payment page
                return "redirect:/appointments/payment/card?appointmentId=" + appointment.getId();
            } else {
                // For CASH, redirect to success page
                redirectAttributes.addFlashAttribute("success", "Appointment booked successfully!");
                redirectAttributes.addFlashAttribute("appointmentId", appointment.getId());
                redirectAttributes.addFlashAttribute("paymentMethod", paymentMethod);
                return "redirect:/appointments/success";
            }
            
        } catch (Exception e) {
            // Release the reservation on error
            reservationService.cancelReservation(patient.getId(), session.getId());
            
            // Provide user-friendly error messages
            String errorMessage = e.getMessage() != null ? e.getMessage() : "";
            
            // Check for MongoDB duplicate key error (race condition at DB level)
            if (e.getClass().getName().contains("DuplicateKey") || errorMessage.contains("duplicate key")) {
                redirectAttributes.addFlashAttribute("error", "Sorry! This time slot was just booked by another patient at the same time. Please select a different time.");
            } else if (errorMessage.contains("just been booked")) {
                redirectAttributes.addFlashAttribute("error", "Sorry! This time slot was just booked by another patient. Please select a different time.");
            } else if (errorMessage.contains("no longer available")) {
                redirectAttributes.addFlashAttribute("error", "This time slot is no longer available. Please select another time.");
            } else if (errorMessage.contains("past date")) {
                redirectAttributes.addFlashAttribute("error", "Cannot book appointments for past dates.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to book appointment. Please try again or select a different time slot.");
                // Log the actual error for debugging
                System.err.println("Appointment booking error: " + e.getClass().getName() + " - " + errorMessage);
                e.printStackTrace();
            }
            
            return "redirect:/appointments/book";
        }
    }
    
    /**
     * Show card payment page for appointment
     */
    @GetMapping("/payment/card")
    public String showCardPayment(
            @RequestParam String appointmentId,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Patient patient = patientRepository.findById(user.getId()).orElse(null);
        if (patient == null) {
            return "redirect:/dashboard";
        }
        
        // Get appointment
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            redirectAttributes.addFlashAttribute("error", "Appointment not found");
            return "redirect:/dashboard";
        }
        
        // Get doctor and hospital
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
        if (doctor == null) {
            redirectAttributes.addFlashAttribute("error", "Doctor not found");
            return "redirect:/dashboard";
        }
        
        Hospital hospital = hospitalRepository.findById(doctor.getHospitalId()).orElse(null);
        if (hospital == null) {
            redirectAttributes.addFlashAttribute("error", "Hospital not found");
            return "redirect:/dashboard";
        }
        
        model.addAttribute("patient", patient);
        model.addAttribute("appointment", appointment);
        model.addAttribute("doctor", doctor);
        model.addAttribute("hospital", hospital);
        
        return "appointments/card-payment";
    }
    
    /**
     * Handle successful payment from Stripe
     */
    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam("session_id") String sessionId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            // Get appointment ID from session
            String appointmentId = (String) session.getAttribute("appointmentId");
            
            if (appointmentId == null) {
                redirectAttributes.addFlashAttribute("error", "Appointment information not found");
                return "redirect:/dashboard";
            }
            
            // Get appointment to retrieve payment amount
            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
            if (appointment == null) {
                redirectAttributes.addFlashAttribute("error", "Appointment not found");
                return "redirect:/dashboard";
            }
            
            // Get doctor and hospital to calculate amount
            Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
            if (doctor == null) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/dashboard";
            }
            
            Hospital hospital = hospitalRepository.findById(doctor.getHospitalId()).orElse(null);
            if (hospital == null) {
                redirectAttributes.addFlashAttribute("error", "Hospital not found");
                return "redirect:/dashboard";
            }
            
            // Create payment record in database
            Payment payment = paymentService.createCardPayment(
                appointmentId,
                sessionId,  // Stripe session ID as transaction ID
                hospital.getHospitalCharges()
            );
            
            // Clear session data
            session.removeAttribute("appointmentId");
            session.removeAttribute("selectedPaymentMethod");
            
            // Set success message
            redirectAttributes.addFlashAttribute("success", "Payment completed successfully!");
            redirectAttributes.addFlashAttribute("appointmentId", appointmentId);
            redirectAttributes.addFlashAttribute("paymentMethod", "CARD");
            redirectAttributes.addFlashAttribute("paymentId", payment.getId());
            
            return "redirect:/appointments/success";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Payment processing failed: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }
    
    /**
     * Handle cancelled payment from Stripe
     */
    @GetMapping("/payment/cancel")
    public String paymentCancel(HttpSession session, RedirectAttributes redirectAttributes) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        String appointmentId = (String) session.getAttribute("appointmentId");
        
        redirectAttributes.addFlashAttribute("error", "Payment was cancelled. Please try again.");
        
        if (appointmentId != null) {
            return "redirect:/appointments/payment/card?appointmentId=" + appointmentId;
        }
        
        return "redirect:/dashboard";
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
    public Map<String, Object> getAvailableSlots(
            @RequestParam String doctorId,
            @RequestParam String date,
            HttpSession session) {
        
        UserDTO user = (UserDTO) session.getAttribute("user");
        String patientId = user != null ? user.getId() : null;
        
        LocalDate selectedDate = LocalDate.parse(date);
        List<LocalTime> availableSlots = appointmentService.getAvailableTimeSlots(doctorId, selectedDate, patientId);
        List<LocalTime> reservedSlots = appointmentService.getReservedTimeSlots(doctorId, selectedDate, patientId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("available", availableSlots);
        response.put("reserved", reservedSlots);
        
        return response;
    }
    
    /**
     * Reserve a time slot temporarily (AJAX endpoint)
     */
    @PostMapping("/reserve-slot")
    @ResponseBody
    public Map<String, Object> reserveTimeSlot(
            @RequestParam String doctorId,
            @RequestParam String date,
            @RequestParam String time,
            HttpSession session) {
        
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "User not logged in");
        }
        
        try {
            LocalDate selectedDate = LocalDate.parse(date);
            LocalTime selectedTime = LocalTime.parse(time);
            LocalDateTime slotDateTime = LocalDateTime.of(selectedDate, selectedTime);
            
            TimeSlotReservation reservation = reservationService.reserveTimeSlot(
                doctorId, 
                slotDateTime, 
                user.getId(), 
                session.getId()
            );
            
            if (reservation == null) {
                return Map.of(
                    "success", false, 
                    "message", "This time slot has just been reserved by another user. Please select a different time."
                );
            }
            
            long remainingSeconds = reservationService.getRemainingSeconds(user.getId(), session.getId());
            
            return Map.of(
                "success", true, 
                "message", "Time slot reserved successfully",
                "reservationId", reservation.getId(),
                "remainingSeconds", remainingSeconds
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", "Failed to reserve slot: " + e.getMessage());
        }
    }
    
    /**
     * Release a reserved time slot (AJAX endpoint)
     */
    @PostMapping("/release-slot")
    @ResponseBody
    public Map<String, Object> releaseTimeSlot(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "User not logged in");
        }
        
        try {
            reservationService.cancelReservation(user.getId(), session.getId());
            return Map.of("success", true, "message", "Reservation cancelled");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    /**
     * Check reservation status (AJAX endpoint)
     */
    @GetMapping("/check-reservation")
    @ResponseBody
    public Map<String, Object> checkReservation(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "User not logged in");
        }
        
        try {
            boolean isValid = reservationService.isReservationValid(user.getId(), session.getId());
            long remainingSeconds = reservationService.getRemainingSeconds(user.getId(), session.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isValid", isValid);
            response.put("remainingSeconds", remainingSeconds);
            
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    /**
     * Download appointment confirmation PDF
     */
    @GetMapping("/download-confirmation/{appointmentId}")
    public ResponseEntity<byte[]> downloadAppointmentConfirmation(
            @PathVariable String appointmentId,
            HttpSession session) {
        
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // Get appointment details
            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify the appointment belongs to the logged-in patient
            if (!appointment.getPatientId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Get patient details
            Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
            if (patient == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get doctor details
            Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
            if (doctor == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get hospital details
            Hospital hospital = hospitalRepository.findById(doctor.getHospitalId()).orElse(null);
            if (hospital == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get payment details if exists (for card payment details in PDF)
            Payment payment = paymentService.getPaymentByAppointmentId(appointmentId).orElse(null);
            
            // Generate PDF
            byte[] pdfBytes = pdfGenerationService.generateAppointmentConfirmationPdf(
                appointment, patient, doctor, hospital, payment);
            
            // Set headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("Appointment_Confirmation_" + appointmentId + ".pdf")
                .build());
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
