package com.example.health_care_system.service;

import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to handle data migrations for Hospital entities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HospitalMigrationService {
    
    private final HospitalRepository hospitalRepository;
    
    // Default charge for private hospitals (can be configured)
    private static final BigDecimal DEFAULT_PRIVATE_HOSPITAL_CHARGE = new BigDecimal("5000.00");
    
    /**
     * Migrate existing hospitals to set hospitalCharges based on their type.
     * - GOVERNMENT hospitals: set to 0
     * - PRIVATE hospitals: set to default charge (5000.00)
     * 
     * @return MigrationResult with statistics
     */
    @Transactional
    public MigrationResult migrateHospitalCharges() {
        log.info("🔄 Starting hospital charges migration...");
        
        List<Hospital> allHospitals = hospitalRepository.findAll();
        int totalHospitals = allHospitals.size();
        int updatedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;
        
        for (Hospital hospital : allHospitals) {
            try {
                // Check if hospitalCharges is already set (not null and not zero for private hospitals)
                if (hospital.getHospitalCharges() != null && 
                    (hospital.getType() == Hospital.HospitalType.GOVERNMENT || 
                     hospital.getHospitalCharges().compareTo(BigDecimal.ZERO) > 0)) {
                    log.debug("   ⏩ Skipping hospital '{}' - already has charges set", hospital.getName());
                    skippedCount++;
                    continue;
                }
                
                // Set charges based on hospital type
                if (hospital.getType() == Hospital.HospitalType.GOVERNMENT) {
                    hospital.setHospitalCharges(BigDecimal.ZERO);
                    log.info("   ✓ Set government hospital '{}' charges to 0", hospital.getName());
                } else if (hospital.getType() == Hospital.HospitalType.PRIVATE) {
                    hospital.setHospitalCharges(DEFAULT_PRIVATE_HOSPITAL_CHARGE);
                    log.info("   ✓ Set private hospital '{}' charges to {}", 
                        hospital.getName(), DEFAULT_PRIVATE_HOSPITAL_CHARGE);
                } else {
                    // Unknown type - set to zero as safe default
                    hospital.setHospitalCharges(BigDecimal.ZERO);
                    log.warn("   ⚠ Hospital '{}' has unknown type, set charges to 0", hospital.getName());
                }
                
                hospital.setUpdatedAt(LocalDateTime.now());
                hospitalRepository.save(hospital);
                updatedCount++;
                
            } catch (Exception e) {
                log.error("   ✗ Error updating hospital '{}': {}", hospital.getName(), e.getMessage());
                errorCount++;
            }
        }
        
        log.info("✅ Hospital charges migration completed!");
        log.info("   Total hospitals: {}", totalHospitals);
        log.info("   Updated: {}", updatedCount);
        log.info("   Skipped (already set): {}", skippedCount);
        log.info("   Errors: {}", errorCount);
        
        return new MigrationResult(totalHospitals, updatedCount, skippedCount, errorCount);
    }
    
    /**
     * Result class for migration operations
     */
    public static class MigrationResult {
        private final int totalRecords;
        private final int updatedRecords;
        private final int skippedRecords;
        private final int errorRecords;
        
        public MigrationResult(int totalRecords, int updatedRecords, int skippedRecords, int errorRecords) {
            this.totalRecords = totalRecords;
            this.updatedRecords = updatedRecords;
            this.skippedRecords = skippedRecords;
            this.errorRecords = errorRecords;
        }
        
        public int getTotalRecords() {
            return totalRecords;
        }
        
        public int getUpdatedRecords() {
            return updatedRecords;
        }
        
        public int getSkippedRecords() {
            return skippedRecords;
        }
        
        public int getErrorRecords() {
            return errorRecords;
        }
        
        public boolean isSuccessful() {
            return errorRecords == 0;
        }
        
        public String getSummary() {
            return String.format(
                "Migration completed: %d total, %d updated, %d skipped, %d errors",
                totalRecords, updatedRecords, skippedRecords, errorRecords
            );
        }
    }
}
