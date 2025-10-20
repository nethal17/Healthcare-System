package com.example.health_care_system.exception;

/**
 * Base exception class for all custom exceptions in the healthcare system.
 * Follows best practices by providing a centralized exception hierarchy.
 */
public abstract class BaseException extends RuntimeException {
    
    public BaseException(String message) {
        super(message);
    }
    
    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
