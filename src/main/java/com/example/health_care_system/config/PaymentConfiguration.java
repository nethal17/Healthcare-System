package com.example.health_care_system.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Configuration class for payment-related constants.
 * Centralizes payment configuration for better maintainability.
 */
@Configuration
@Getter
public class PaymentConfiguration {
    
    @Value("${payment.consultation.fee:2500.00}")
    private BigDecimal consultationFee;
    
    @Value("${payment.currency:LKR}")
    private String currency;
    
    @Value("${payment.stripe.success.url:${app.base.url}/appointments/payment/success}")
    private String stripeSuccessUrl;
    
    @Value("${payment.stripe.cancel.url:${app.base.url}/appointments/payment/cancel}")
    private String stripeCancelUrl;
}
