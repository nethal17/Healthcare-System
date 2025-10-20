package com.example.health_care_system.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Provides clear semantics for duplicate entry scenarios.
 */
public class DuplicateResourceException extends BaseException {
    
    public DuplicateResourceException(String resourceName, String field, String value) {
        super(String.format("%s already exists with %s: %s", resourceName, field, value));
    }
    
    public DuplicateResourceException(String message) {
        super(message);
    }
}
