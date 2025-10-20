package com.example.health_care_system.exception;

/**
 * Exception thrown for authentication-related failures.
 * Separates authentication concerns from general exceptions.
 */
public class AuthenticationException extends BaseException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
