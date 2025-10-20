package com.example.health_care_system.strategy;

import com.example.health_care_system.model.Payment;

import java.math.BigDecimal;

/**
 * Strategy interface for different payment methods.
 * Follows Strategy Pattern and Open/Closed Principle.
 * Allows adding new payment methods without modifying existing code.
 */
public interface PaymentStrategy {
    
    /**
     * Create a payment record for specific payment method
     * 
     * @param appointmentId Appointment identifier
     * @param amount Payment amount
     * @param additionalData Additional data specific to payment method
     * @return Created Payment entity
     */
    Payment createPayment(String appointmentId, BigDecimal amount, PaymentContext additionalData);
    
    /**
     * Get the payment method type this strategy handles
     */
    Payment.PaymentMethod getPaymentMethod();
}
