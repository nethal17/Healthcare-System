package com.example.health_care_system.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for centralized exception handling.
 * Follows Single Responsibility Principle - handles only exception mapping.
 * Provides consistent error responses across the application.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(
            ResourceNotFoundException ex,
            RedirectAttributes redirectAttributes) {
        log.error("Resource not found: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dashboard";
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(
            UserNotFoundException ex,
            RedirectAttributes redirectAttributes) {
        log.error("User not found: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/login";
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public String handleAuthenticationException(
            AuthenticationException ex,
            RedirectAttributes redirectAttributes) {
        log.warn("Authentication failed: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/login";
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public String handleDuplicateResourceException(
            DuplicateResourceException ex,
            RedirectAttributes redirectAttributes) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/register";
    }
    
    @ExceptionHandler(ValidationException.class)
    public String handleValidationException(
            ValidationException ex,
            RedirectAttributes redirectAttributes) {
        log.warn("Validation error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dashboard";
    }
    
    @ExceptionHandler(BusinessLogicException.class)
    public String handleBusinessLogicException(
            BusinessLogicException ex,
            RedirectAttributes redirectAttributes) {
        log.warn("Business logic violation: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dashboard";
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(
            Exception ex,
            RedirectAttributes redirectAttributes) {
        log.error("Unexpected error occurred: ", ex);
        redirectAttributes.addFlashAttribute("error", 
            "An unexpected error occurred. Please try again later.");
        return "redirect:/dashboard";
    }
}
