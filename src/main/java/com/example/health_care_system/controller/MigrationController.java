package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.service.HospitalMigrationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for administrative migration and maintenance operations
 * Requires ADMIN role to access
 */
@RestController
@RequestMapping("/api/admin/migration")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {
    
    private final HospitalMigrationService hospitalMigrationService;
    
    /**
     * Endpoint to trigger hospital charges migration
     * Sets hospitalCharges for all existing hospitals based on their type
     * 
     * @return Migration result with statistics
     */
    @PostMapping("/hospital-charges")
    public ResponseEntity<Map<String, Object>> migrateHospitalCharges(HttpSession session) {
        // Check if user is admin
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null || user.getRole() != UserRole.ADMIN) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Unauthorized. Admin access required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        log.info("🔧 Admin '{}' triggered hospital charges migration", user.getName());
        
        try {
            HospitalMigrationService.MigrationResult result = 
                hospitalMigrationService.migrateHospitalCharges();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccessful());
            response.put("message", result.getSummary());
            response.put("totalRecords", result.getTotalRecords());
            response.put("updatedRecords", result.getUpdatedRecords());
            response.put("skippedRecords", result.getSkippedRecords());
            response.put("errorRecords", result.getErrorRecords());
            
            if (result.isSuccessful()) {
                log.info("✅ Hospital charges migration completed successfully");
                return ResponseEntity.ok(response);
            } else {
                log.warn("⚠️ Hospital charges migration completed with errors");
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }
            
        } catch (Exception e) {
            log.error("❌ Hospital charges migration failed: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Migration failed: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint to verify migration service is available
     * 
     * @return Simple status response
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus(HttpSession session) {
        // Check if user is admin
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null || user.getRole() != UserRole.ADMIN) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unauthorized. Admin access required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        Map<String, String> status = new HashMap<>();
        status.put("service", "Migration Service");
        status.put("status", "Available");
        status.put("version", "1.0");
        return ResponseEntity.ok(status);
    }
}
