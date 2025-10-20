package com.example.health_care_system.exception;

/**
 * Exception thrown when email sending fails.
 * This is a runtime exception to avoid forcing callers to handle it.
 */
public class EmailSendException extends RuntimeException {
    
    public EmailSendException(String message) {
        super(message);
    }
    
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
