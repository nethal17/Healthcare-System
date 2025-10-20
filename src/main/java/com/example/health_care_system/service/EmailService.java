package com.example.health_care_system.service;

import com.example.health_care_system.builder.EmailTemplateBuilder;
import com.example.health_care_system.exception.EmailSendException;
import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Payment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for sending email notifications.
 * Uses EmailTemplateBuilder to eliminate code duplication.
 * Follows SOLID principles with constructor injection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailTemplateBuilder emailTemplateBuilder;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    /**
     * Send appointment confirmation email for government hospital (free service)
     */
    public void sendGovernmentAppointmentConfirmation(
            Patient patient,
            Appointment appointment,
            Doctor doctor,
            Hospital hospital) {
        
        log.debug("Sending government hospital appointment confirmation to: {}", patient.getEmail());
        
        try {
            // Build email context using builder
            Context context = emailTemplateBuilder.buildAppointmentContext(
                    patient, appointment, doctor, hospital);
            
            // Process template
            String htmlContent = templateEngine.process(
                    "emails/appointment-confirmation-government", context);
            
            // Send email
            sendEmail(
                    patient.getEmail(),
                    "Appointment Confirmation - " + hospital.getName(),
                    htmlContent
            );
            
            log.info("Government hospital appointment confirmation sent successfully to {} for appointment {}",
                    patient.getEmail(), appointment.getId());
            
        } catch (MessagingException e) {
            log.error("Failed to send government appointment confirmation to {} for appointment {}: {}",
                    patient.getEmail(), appointment.getId(), e.getMessage(), e);
            throw new EmailSendException(
                    "Failed to send government appointment confirmation email", e);
        }
    }
    
    /**
     * Send appointment confirmation email for cash payment
     */
    public void sendCashPaymentAppointmentConfirmation(
            Patient patient,
            Appointment appointment,
            Doctor doctor,
            Hospital hospital,
            Payment payment) {
        
        log.debug("Sending cash payment appointment confirmation to: {}", patient.getEmail());
        
        try {
            // Build email context using builder
            Context context = emailTemplateBuilder.buildAppointmentContext(
                    patient, appointment, doctor, hospital);
            
            // Add payment information
            emailTemplateBuilder.addPaymentInfo(context, payment);
            
            // Process template
            String htmlContent = templateEngine.process(
                    "emails/appointment-confirmation-cash", context);
            
            // Send email
            sendEmail(
                    patient.getEmail(),
                    "Appointment Confirmation - Payment Required at Hospital",
                    htmlContent
            );
            
            log.info("Cash payment appointment confirmation sent successfully to {} for appointment {} with payment {}",
                    patient.getEmail(), appointment.getId(), payment.getId());
            
        } catch (MessagingException e) {
            log.error("Failed to send cash payment appointment confirmation to {} for appointment {}: {}",
                    patient.getEmail(), appointment.getId(), e.getMessage(), e);
            throw new EmailSendException(
                    "Failed to send cash payment appointment confirmation email", e);
        }
    }
    
    /**
     * Send appointment confirmation email for card payment
     */
    public void sendCardPaymentAppointmentConfirmation(
            Patient patient,
            Appointment appointment,
            Doctor doctor,
            Hospital hospital,
            Payment payment) {
        
        log.debug("Sending card payment appointment confirmation to: {}", patient.getEmail());
        
        try {
            // Build email context using builder
            Context context = emailTemplateBuilder.buildAppointmentContext(
                    patient, appointment, doctor, hospital);
            
            // Add payment information
            emailTemplateBuilder.addPaymentInfo(context, payment);
            
            // Process template
            String htmlContent = templateEngine.process(
                    "emails/appointment-confirmation-card", context);
            
            // Send email
            sendEmail(
                    patient.getEmail(),
                    "Appointment Confirmation - Payment Successful",
                    htmlContent
            );
            
            log.info("Card payment appointment confirmation sent successfully to {} for appointment {} with payment {}",
                    patient.getEmail(), appointment.getId(), payment.getId());
            
        } catch (MessagingException e) {
            log.error("Failed to send card payment appointment confirmation to {} for appointment {}: {}",
                    patient.getEmail(), appointment.getId(), e.getMessage(), e);
            throw new EmailSendException(
                    "Failed to send card payment appointment confirmation email", e);
        }
    }
    
    /**
     * Send appointment confirmation email for insurance claim
     */
    public void sendInsuranceAppointmentConfirmation(
            Patient patient,
            Appointment appointment,
            Doctor doctor,
            Hospital hospital,
            Payment payment) {
        
        log.debug("Sending insurance appointment confirmation to: {}", patient.getEmail());
        
        try {
            // Build email context using builder
            Context context = emailTemplateBuilder.buildAppointmentContext(
                    patient, appointment, doctor, hospital);
            
            // Add insurance information
            emailTemplateBuilder.addInsuranceInfo(context, payment);
            
            // Process template
            String htmlContent = templateEngine.process(
                    "emails/appointment-confirmation-insurance", context);
            
            // Send email
            sendEmail(
                    patient.getEmail(),
                    "Appointment Confirmation - Insurance Claim Pending",
                    htmlContent
            );
            
            log.info("Insurance appointment confirmation sent successfully to {} for appointment {} with payment {}",
                    patient.getEmail(), appointment.getId(), payment.getId());
            
        } catch (MessagingException e) {
            log.error("Failed to send insurance appointment confirmation to {} for appointment {}: {}",
                    patient.getEmail(), appointment.getId(), e.getMessage(), e);
            throw new EmailSendException(
                    "Failed to send insurance appointment confirmation email", e);
        }
    }
    
    /**
     * Helper method to send email
     */
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        log.debug("Preparing to send email to: {} with subject: {}", to, subject);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
        
        log.debug("Email sent successfully to: {}", to);
    }
}
