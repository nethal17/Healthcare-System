package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Payment;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.HospitalRepository;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.service.AppointmentService;
import com.example.health_care_system.service.PaymentService;
import com.example.health_care_system.service.PdfGenerationService;
import com.example.health_care_system.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller

public class PaymentController {

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private HospitalRepository hospitalRepository;
    
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PdfGenerationService pdfGenerationService;
    
    @Autowired
    private EmailService emailService;

    @GetMapping
    public String index(){
        return "index";
    }

    @GetMapping("/success")
    public String success(){
        return "success";
    }

    @GetMapping("/cancel")
    public String cancel(){
        return "cancel";
    }

    @GetMapping("/InsuranceCollection")
    public String InsuranceCollection(Model model, HttpSession session) {
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
        Doctor doctor = doctorRepository.findById(appointmentDetails.get("doctorId")).orElse(null);
        if (doctor != null) {
            Hospital hospital = hospitalRepository.findById(doctor.getHospitalId()).orElse(null);
            model.addAttribute("hospital", hospital);
            model.addAttribute("doctor", doctor);
        }
        
        model.addAttribute("patient", patient);
        model.addAttribute("appointmentDetails", appointmentDetails);
        
        return "appointments/insurance-form";
    }
    
    @PostMapping("/InsuranceCollection/process")
    public String processInsuranceCollection(
            @RequestParam String insuranceProvider,
            @RequestParam String policyNumber,
            @RequestParam(required = false) String policyHolderName,
            @RequestParam(required = false) String relationshipToPatient,
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
            
            // Get doctor and hospital for payment record
            Doctor doctor = doctorRepository.findById(appointmentDetails.get("doctorId")).orElse(null);
            if (doctor != null) {
                Hospital hospital = hospitalRepository.findById(doctor.getHospitalId()).orElse(null);
                if (hospital != null && hospital.getHospitalCharges() != null) {
                    // Create payment record for insurance
                    Payment payment = paymentService.createInsurancePayment(
                        appointment.getId(),
                        hospital.getHospitalCharges(),
                        insuranceProvider,
                        policyNumber
                    );
                    session.setAttribute("paymentId", payment.getId());
                    
                    // Send confirmation email for insurance payment
                    try {
                        emailService.sendInsuranceAppointmentConfirmation(patient, appointment, doctor, hospital, payment);
                    } catch (Exception e) {
                        // Log email error but don't fail the appointment
                        System.err.println("Failed to send confirmation email: " + e.getMessage());
                    }
                }
            }
            
            // Store insurance information in session
            session.setAttribute("appointmentId", appointment.getId());
            session.setAttribute("insuranceProvider", insuranceProvider);
            session.setAttribute("policyNumber", policyNumber);
            session.setAttribute("policyHolderName", policyHolderName);
            session.setAttribute("relationshipToPatient", relationshipToPatient);
            
            // Clear pending appointment from session
            session.removeAttribute("pendingAppointment");
            
            // Redirect to pending insurance request page
            return "redirect:/PendingInsuranceRequest";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to process insurance: " + e.getMessage());
            return "redirect:/InsuranceCollection";
        }
    }

    @GetMapping("/PendingInsuranceRequest")
    public String PendingInsuranceRequest(Model model, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Patient patient = patientRepository.findById(user.getId()).orElse(null);
        if (patient == null) {
            return "redirect:/dashboard";
        }
        
        // Get appointment and insurance details from session
        String appointmentId = (String) session.getAttribute("appointmentId");
        String insuranceProvider = (String) session.getAttribute("insuranceProvider");
        String policyNumber = (String) session.getAttribute("policyNumber");
        
        if (appointmentId != null) {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
            model.addAttribute("appointment", appointment);
            
            if (appointment != null) {
                Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
                if (doctor != null) {
                    Hospital hospital = hospitalRepository.findById(doctor.getHospitalId()).orElse(null);
                    model.addAttribute("hospital", hospital);
                    model.addAttribute("doctor", doctor);
                }
            }
        }
        
        model.addAttribute("patient", patient);
        model.addAttribute("insuranceProvider", insuranceProvider);
        model.addAttribute("policyNumber", policyNumber);
        
        return "PendingInsuranceRequest";
    }

    /**
     * Download insurance appointment confirmation PDF
     */
    @GetMapping("/appointments/download-insurance-confirmation/{appointmentId}")
    public ResponseEntity<byte[]> downloadInsuranceConfirmation(
            @PathVariable String appointmentId,
            HttpSession session) {
        
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            // Get appointment
            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }

            // Get patient
            Patient patient = patientRepository.findById(user.getId()).orElse(null);
            if (patient == null) {
                return ResponseEntity.notFound().build();
            }

            // Get doctor and hospital
            Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
            Hospital hospital = null;
            if (doctor != null) {
                hospital = hospitalRepository.findById(doctor.getHospitalId()).orElse(null);
            }

            // Get insurance information from session
            String insuranceProvider = (String) session.getAttribute("insuranceProvider");
            String policyNumber = (String) session.getAttribute("policyNumber");

            // Generate PDF
            byte[] pdfBytes = pdfGenerationService.generateInsuranceAppointmentPdf(
                    appointment,
                    patient,
                    doctor,
                    hospital,
                    insuranceProvider,
                    policyNumber
            );

            // Create filename with appointment date
            String dateStr = appointment.getAppointmentDateTime() != null ?
                    appointment.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) :
                    LocalDate.now().toString();
            String filename = "Insurance_Appointment_" + appointmentId + "_" + dateStr + ".pdf";

            // Return PDF as download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
