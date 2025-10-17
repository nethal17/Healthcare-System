package com.example.health_care_system.service;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Payment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.base.url}")
    private String baseUrl;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    
    /**
     * Send appointment confirmation email for government hospital (free service)
     */
    public void sendGovernmentAppointmentConfirmation(
            Patient patient,
            Appointment appointment,
            Doctor doctor,
            Hospital hospital) throws MessagingException {
        
        Context context = new Context();
        context.setVariable("patientName", patient.getName());
        context.setVariable("appointmentId", appointment.getId());
        context.setVariable("appointmentDate", appointment.getAppointmentDateTime().format(DATE_FORMATTER));
        context.setVariable("appointmentTime", appointment.getAppointmentDateTime().format(TIME_FORMATTER));
        context.setVariable("doctorName", doctor.getName());
        context.setVariable("doctorSpecialization", doctor.getSpecialization());
        context.setVariable("hospitalName", hospital.getName());
        context.setVariable("hospitalAddress", hospital.getLocation().getAddress());
        context.setVariable("hospitalCity", hospital.getLocation().getCity());
        context.setVariable("hospitalPhone", hospital.getContactInfo().getPhoneNumber());
        context.setVariable("hospitalEmail", hospital.getContactInfo().getEmail());
        context.setVariable("purpose", appointment.getPurpose() != null && !appointment.getPurpose().isEmpty() 
                ? appointment.getPurpose() : "General Consultation");
        context.setVariable("currentYear", String.valueOf(LocalDateTime.now().getYear()));
        context.setVariable("baseUrl", baseUrl);
        
        String htmlContent = templateEngine.process("emails/appointment-confirmation-government", context);
        
        sendEmail(
            patient.getEmail(),
            "Appointment Confirmation - " + hospital.getName(),
            htmlContent
        );
    }
    
    /**
     * Send appointment confirmation email for cash payment
     */
    public void sendCashPaymentAppointmentConfirmation(
            Patient patient,
            Appointment appointment,
            Doctor doctor,
            Hospital hospital,
            Payment payment) throws MessagingException {
        
        Context context = new Context();
        context.setVariable("patientName", patient.getName());
        context.setVariable("appointmentId", appointment.getId());
        context.setVariable("appointmentDate", appointment.getAppointmentDateTime().format(DATE_FORMATTER));
        context.setVariable("appointmentTime", appointment.getAppointmentDateTime().format(TIME_FORMATTER));
        context.setVariable("doctorName", doctor.getName());
        context.setVariable("doctorSpecialization", doctor.getSpecialization());
        context.setVariable("hospitalName", hospital.getName());
        context.setVariable("hospitalAddress", hospital.getLocation().getAddress());
        context.setVariable("hospitalCity", hospital.getLocation().getCity());
        context.setVariable("hospitalPhone", hospital.getContactInfo().getPhoneNumber());
        context.setVariable("hospitalEmail", hospital.getContactInfo().getEmail());
        context.setVariable("purpose", appointment.getPurpose() != null && !appointment.getPurpose().isEmpty() 
                ? appointment.getPurpose() : "General Consultation");
        context.setVariable("amount", formatCurrency(payment.getAmount()));
        context.setVariable("paymentId", payment.getId());
        context.setVariable("currentYear", String.valueOf(LocalDateTime.now().getYear()));
        context.setVariable("baseUrl", baseUrl);
        
        String htmlContent = templateEngine.process("emails/appointment-confirmation-cash", context);
        
        sendEmail(
            patient.getEmail(),
            "Appointment Confirmation - Payment Required at Hospital",
            htmlContent
        );
    }
    
    /**
     * Send appointment confirmation email for card payment
     */
    public void sendCardPaymentAppointmentConfirmation(
            Patient patient,
            Appointment appointment,
            Doctor doctor,
            Hospital hospital,
            Payment payment) throws MessagingException {
        
        Context context = new Context();
        context.setVariable("patientName", patient.getName());
        context.setVariable("appointmentId", appointment.getId());
        context.setVariable("appointmentDate", appointment.getAppointmentDateTime().format(DATE_FORMATTER));
        context.setVariable("appointmentTime", appointment.getAppointmentDateTime().format(TIME_FORMATTER));
        context.setVariable("doctorName", doctor.getName());
        context.setVariable("doctorSpecialization", doctor.getSpecialization());
        context.setVariable("hospitalName", hospital.getName());
        context.setVariable("hospitalAddress", hospital.getLocation().getAddress());
        context.setVariable("hospitalCity", hospital.getLocation().getCity());
        context.setVariable("hospitalPhone", hospital.getContactInfo().getPhoneNumber());
        context.setVariable("hospitalEmail", hospital.getContactInfo().getEmail());
        context.setVariable("purpose", appointment.getPurpose() != null && !appointment.getPurpose().isEmpty() 
                ? appointment.getPurpose() : "General Consultation");
        context.setVariable("amount", formatCurrency(payment.getAmount()));
        context.setVariable("paymentId", payment.getId());
        context.setVariable("transactionId", payment.getTransactionId());
        context.setVariable("paymentDate", payment.getCreatedAt().format(DATE_FORMATTER));
        context.setVariable("currentYear", String.valueOf(LocalDateTime.now().getYear()));
        context.setVariable("baseUrl", baseUrl);
        
        String htmlContent = templateEngine.process("emails/appointment-confirmation-card", context);
        
        sendEmail(
            patient.getEmail(),
            "Appointment Confirmation - Payment Successful",
            htmlContent
        );
    }
    
    /**
     * Send appointment confirmation email for insurance claim
     */
    public void sendInsuranceAppointmentConfirmation(
            Patient patient,
            Appointment appointment,
            Doctor doctor,
            Hospital hospital,
            Payment payment) throws MessagingException {
        
        Context context = new Context();
        context.setVariable("patientName", patient.getName());
        context.setVariable("appointmentId", appointment.getId());
        context.setVariable("appointmentDate", appointment.getAppointmentDateTime().format(DATE_FORMATTER));
        context.setVariable("appointmentTime", appointment.getAppointmentDateTime().format(TIME_FORMATTER));
        context.setVariable("doctorName", doctor.getName());
        context.setVariable("doctorSpecialization", doctor.getSpecialization());
        context.setVariable("hospitalName", hospital.getName());
        context.setVariable("hospitalAddress", hospital.getLocation().getAddress());
        context.setVariable("hospitalCity", hospital.getLocation().getCity());
        context.setVariable("hospitalPhone", hospital.getContactInfo().getPhoneNumber());
        context.setVariable("hospitalEmail", hospital.getContactInfo().getEmail());
        context.setVariable("purpose", appointment.getPurpose() != null && !appointment.getPurpose().isEmpty() 
                ? appointment.getPurpose() : "General Consultation");
        context.setVariable("amount", formatCurrency(payment.getAmount()));
        context.setVariable("insuranceProvider", payment.getInsuranceProvider());
        context.setVariable("policyNumber", payment.getInsurancePolicyNumber());
        context.setVariable("paymentId", payment.getId());
        context.setVariable("currentYear", String.valueOf(LocalDateTime.now().getYear()));
        context.setVariable("baseUrl", baseUrl);
        
        String htmlContent = templateEngine.process("emails/appointment-confirmation-insurance", context);
        
        sendEmail(
            patient.getEmail(),
            "Appointment Confirmation - Insurance Claim Pending",
            htmlContent
        );
    }
    
    /**
     * Helper method to send email
     */
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
    
    /**
     * Format currency amount
     */
    private String formatCurrency(BigDecimal amount) {
        return String.format("LKR %.2f", amount);
    }
}
