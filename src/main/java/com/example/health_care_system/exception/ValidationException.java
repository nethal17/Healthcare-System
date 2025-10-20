package com.example.health_care_system.exception;

/**
 * Exception thrown for validation failures.
 * Provides clear separation of validation logic from business logic.
 */
public class ValidationException extends BaseException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String field, String violation) {
        super(String.format("Validation failed for %s: %s", field, violation));
    }
}
