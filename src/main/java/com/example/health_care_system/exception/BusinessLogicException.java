package com.example.health_care_system.exception;

/**
 * Exception thrown for business logic violations.
 * Used when business rules are not satisfied.
 */
public class BusinessLogicException extends BaseException {
    
    public BusinessLogicException(String message) {
        super(message);
    }
    
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}
