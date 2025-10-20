package com.example.health_care_system.service;

import com.example.health_care_system.exception.ResourceNotFoundException;
import com.example.health_care_system.factory.PaymentStrategyFactory;
import com.example.health_care_system.model.Payment;
import com.example.health_care_system.repository.PaymentRepository;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.strategy.PaymentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;
    
    /**
     * Create a payment record using Strategy Pattern
     * This method replaces createCardPayment(), createCashPayment(), and createInsurancePayment()
     * 
     * @param appointmentId The appointment ID
     * @param paymentMethod The payment method (CARD, CASH, INSURANCE, etc.)
     * @param amount The payment amount
     * @param context PaymentContext containing additional payment information (transactionId, insurance details, etc.)
     * @return The created Payment object
     */
    @Transactional
    public Payment createPayment(String appointmentId, Payment.PaymentMethod paymentMethod, 
                                BigDecimal amount, PaymentContext context) {
        log.debug("Creating {} payment for appointment ID: {}", paymentMethod, appointmentId);
        
        // Validate appointment exists
        appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));
        
        // Execute payment strategy (strategy handles saving)
        Payment payment = paymentStrategyFactory.getStrategy(paymentMethod)
                .createPayment(appointmentId, amount, context);
        
        log.info("Payment created successfully with ID: {} for appointment: {}", 
                payment.getId(), appointmentId);
        
        return payment;
    }
    
    /**
     * Get payment by appointment ID
     */
    public Optional<Payment> getPaymentByAppointmentId(String appointmentId) {
        log.debug("Fetching payment for appointment ID: {}", appointmentId);
        return paymentRepository.findByAppointmentId(appointmentId);
    }
    
    /**
     * Get all payments for a patient
     */
    public List<Payment> getPaymentsByPatientId(String patientId) {
        log.debug("Fetching payments for patient ID: {}", patientId);
        return paymentRepository.findByPatientId(patientId);
    }
    
    /**
     * Get payment by transaction ID
     */
    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        log.debug("Fetching payment by transaction ID: {}", transactionId);
        return paymentRepository.findByTransactionId(transactionId);
    }
    
    /**
     * Update payment status
     */
    @Transactional
    public Payment updatePaymentStatus(String paymentId, Payment.PaymentStatus status) {
        log.debug("Updating payment status for ID: {} to {}", paymentId, status);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment status updated successfully for ID: {}", paymentId);
        
        return updatedPayment;
    }
    
    /**
     * Get all payments by hospital
     */
    public List<Payment> getPaymentsByHospitalId(String hospitalId) {
        log.debug("Fetching payments for hospital ID: {}", hospitalId);
        return paymentRepository.findByHospitalId(hospitalId);
    }
    
    /**
     * Get all payments by doctor
     */
    public List<Payment> getPaymentsByDoctorId(String doctorId) {
        log.debug("Fetching payments for doctor ID: {}", doctorId);
        return paymentRepository.findByDoctorId(doctorId);
    }
}
