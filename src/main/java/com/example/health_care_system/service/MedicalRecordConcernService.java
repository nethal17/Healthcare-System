package com.example.health_care_system.service;

import com.example.health_care_system.model.MedicalRecordConcern;
import com.example.health_care_system.repository.MedicalRecordConcernRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordConcernService {
    
    private final MedicalRecordConcernRepository concernRepository;
    
    /**
     * Create a new concern
     */
    public MedicalRecordConcern createConcern(MedicalRecordConcern concern) {
        concern.setStatus("PENDING");
        concern.setCreatedAt(LocalDateTime.now());
        log.info("Creating new concern for patient: {} on medical record: {}", 
                concern.getPatientName(), concern.getMedicalRecordId());
        return concernRepository.save(concern);
    }
    
    /**
     * Get all concerns ordered by creation date
     */
    public List<MedicalRecordConcern> getAllConcerns() {
        log.info("Fetching all medical record concerns");
        return concernRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get concerns by patient ID
     */
    public List<MedicalRecordConcern> getConcernsByPatientId(String patientId) {
        log.info("Fetching concerns for patient ID: {}", patientId);
        return concernRepository.findByPatientId(patientId);
    }
    
    /**
     * Get concerns by status
     */
    public List<MedicalRecordConcern> getConcernsByStatus(String status) {
        log.info("Fetching concerns with status: {}", status);
        return concernRepository.findByStatus(status);
    }
    
    /**
     * Get concern by ID
     */
    public Optional<MedicalRecordConcern> getConcernById(String id) {
        return concernRepository.findById(id);
    }
    
    /**
     * Update concern with reply
     */
    public MedicalRecordConcern replyConcern(String concernId, String replyText, String repliedBy) {
        Optional<MedicalRecordConcern> concernOpt = concernRepository.findById(concernId);
        
        if (concernOpt.isPresent()) {
            MedicalRecordConcern concern = concernOpt.get();
            concern.setReplyText(replyText);
            concern.setStatus("REPLIED");
            concern.setRepliedAt(LocalDateTime.now());
            concern.setRepliedBy(repliedBy);
            
            log.info("Replying to concern ID: {} by {}", concernId, repliedBy);
            return concernRepository.save(concern);
        }
        
        log.error("Concern not found with ID: {}", concernId);
        throw new RuntimeException("Concern not found");
    }
    
    /**
     * Delete a concern
     */
    public void deleteConcern(String concernId) {
        log.info("Deleting concern with ID: {}", concernId);
        concernRepository.deleteById(concernId);
    }
}
