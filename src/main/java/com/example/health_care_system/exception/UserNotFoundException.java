package com.example.health_care_system.exception;

/**
 * Specific exception for user-related not found scenarios.
 * Improves code readability and exception handling specificity.
 */
public class UserNotFoundException extends ResourceNotFoundException {
    
    public UserNotFoundException(String identifier) {
        super("User", identifier);
    }
    
    public UserNotFoundException(String message, String identifier) {
        super(String.format("%s: %s", message, identifier));
    }
}
