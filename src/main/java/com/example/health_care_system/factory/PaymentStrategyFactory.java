package com.example.health_care_system.factory;

import com.example.health_care_system.model.Payment;
import com.example.health_care_system.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for managing and retrieving payment strategies.
 * Follows Factory Pattern and Strategy Pattern.
 * Provides easy extensibility for new payment methods.
 */
@Component
public class PaymentStrategyFactory {
    
    private final Map<Payment.PaymentMethod, PaymentStrategy> strategyMap;
    
    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        this.strategyMap = strategies.stream()
            .collect(Collectors.toMap(
                PaymentStrategy::getPaymentMethod,
                Function.identity()
            ));
    }
    
    /**
     * Get payment strategy for specific payment method
     * 
     * @param paymentMethod Payment method type
     * @return PaymentStrategy implementation
     * @throws IllegalArgumentException if strategy not found
     */
    public PaymentStrategy getStrategy(Payment.PaymentMethod paymentMethod) {
        PaymentStrategy strategy = strategyMap.get(paymentMethod);
        if (strategy == null) {
            throw new IllegalArgumentException(
                "No payment strategy found for method: " + paymentMethod
            );
        }
        return strategy;
    }
}
