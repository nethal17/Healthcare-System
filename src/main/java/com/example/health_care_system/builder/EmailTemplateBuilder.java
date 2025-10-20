package com.example.health_care_system.builder;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Builder class for creating email template contexts.
 * Follows Builder Pattern to construct complex email contexts.
 * Eliminates code duplication in EmailService.
 */
@Component
public class EmailTemplateBuilder {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("hh:mm a");
    
    @Value("${app.base.url}")
    private String baseUrl;
    
    /**
     * Build context for appointment confirmation emails
     */
    public Context buildAppointmentContext(Patient patient, Appointment appointment, 
                                          Doctor doctor, Hospital hospital) {
        Context context = new Context();
        
        // Patient information
        context.setVariable("patientName", patient.getName());
        
        // Appointment information
        context.setVariable("appointmentId", appointment.getId());
        context.setVariable("appointmentDate", 
            appointment.getAppointmentDateTime().format(DATE_FORMATTER));
        context.setVariable("appointmentTime", 
            appointment.getAppointmentDateTime().format(TIME_FORMATTER));
        context.setVariable("purpose", 
            appointment.getPurpose() != null && !appointment.getPurpose().isEmpty() 
                ? appointment.getPurpose() : "General Consultation");
        
        // Doctor information
        context.setVariable("doctorName", doctor.getName());
        context.setVariable("doctorSpecialization", doctor.getSpecialization());
        
        // Hospital information
        context.setVariable("hospitalName", hospital.getName());
        context.setVariable("hospitalAddress", hospital.getLocation().getAddress());
        context.setVariable("hospitalCity", hospital.getLocation().getCity());
        context.setVariable("hospitalPhone", hospital.getContactInfo().getPhoneNumber());
        context.setVariable("hospitalEmail", hospital.getContactInfo().getEmail());
        
        // Common variables
        context.setVariable("currentYear", String.valueOf(LocalDateTime.now().getYear()));
        context.setVariable("baseUrl", baseUrl);
        
        return context;
    }
    
    /**
     * Add payment information to existing context
     */
    public Context addPaymentInfo(Context context, Payment payment) {
        context.setVariable("amount", formatCurrency(payment.getAmount()));
        context.setVariable("paymentId", payment.getId());
        
        if (payment.getTransactionId() != null) {
            context.setVariable("transactionId", payment.getTransactionId());
        }
        
        if (payment.getCreatedAt() != null) {
            context.setVariable("paymentDate", payment.getCreatedAt().format(DATE_FORMATTER));
        }
        
        return context;
    }
    
    /**
     * Add insurance information to existing context
     */
    public Context addInsuranceInfo(Context context, Payment payment) {
        // Add base payment info
        addPaymentInfo(context, payment);
        
        // Add insurance-specific fields
        if (payment.getInsuranceProvider() != null) {
            context.setVariable("insuranceProvider", payment.getInsuranceProvider());
        }
        
        if (payment.getInsurancePolicyNumber() != null) {
            context.setVariable("policyNumber", payment.getInsurancePolicyNumber());
        }
        
        return context;
    }
    
    /**
     * Format currency amount
     */
    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));
        return formatter.format(amount);
    }
}
