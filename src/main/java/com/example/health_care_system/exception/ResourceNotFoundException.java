package com.example.health_care_system.exception;

/**
 * Exception thrown when a requested resource is not found in the database.
 * This provides more semantic meaning than generic RuntimeException.
 */
public class ResourceNotFoundException extends BaseException {
    
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier));
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
