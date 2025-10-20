package com.example.health_care_system.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Context object containing additional payment information.
 * Provides a flexible way to pass payment-specific data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentContext {
    private String transactionId;
    private String insuranceProvider;
    private String policyNumber;
    private String notes;
}
