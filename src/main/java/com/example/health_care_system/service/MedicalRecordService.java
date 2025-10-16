package com.example.health_care_system.service;

import com.example.health_care_system.model.MedicalRecord;
import com.example.health_care_system.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {
    
    private final MedicalRecordRepository medicalRecordRepository;
    
    /**
     * Get all medical records for a specific patient
     * @param patientId The patient's ID
     * @return List of medical records ordered by date (most recent first)
     */
    public List<MedicalRecord> getPatientMedicalRecords(String patientId) {
        log.info("Fetching medical records for patient ID: {}", patientId);
        return medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patientId);
    }
    
    /**
     * Get all medical records created by a specific doctor
     * @param doctorId The doctor's ID
     * @return List of medical records
     */
    public List<MedicalRecord> getDoctorMedicalRecords(String doctorId) {
        log.info("Fetching medical records for doctor ID: {}", doctorId);
        return medicalRecordRepository.findByDoctorId(doctorId);
    }
    
    /**
     * Get a specific medical record by ID
     * @param recordId The record ID
     * @return Optional containing the medical record if found
     */
    public Optional<MedicalRecord> getMedicalRecordById(String recordId) {
        log.info("Fetching medical record with ID: {}", recordId);
        return medicalRecordRepository.findById(recordId);
    }
    
    /**
     * Create a new medical record
     * @param medicalRecord The medical record to create
     * @return The saved medical record
     */
    public MedicalRecord createMedicalRecord(MedicalRecord medicalRecord) {
        log.info("Creating new medical record for patient: {}", medicalRecord.getPatientName());
        medicalRecord.setCreatedAt(LocalDateTime.now());
        medicalRecord.setUpdatedAt(LocalDateTime.now());
        return medicalRecordRepository.save(medicalRecord);
    }
    
    /**
     * Update an existing medical record
     * @param recordId The record ID
     * @param updatedRecord The updated medical record data
     * @return The updated medical record
     */
    public Optional<MedicalRecord> updateMedicalRecord(String recordId, MedicalRecord updatedRecord) {
        log.info("Updating medical record with ID: {}", recordId);
        return medicalRecordRepository.findById(recordId).map(existingRecord -> {
            existingRecord.setDiagnosis(updatedRecord.getDiagnosis());
            existingRecord.setPrescription(updatedRecord.getPrescription());
            existingRecord.setNotes(updatedRecord.getNotes());
            existingRecord.setUpdatedAt(LocalDateTime.now());
            return medicalRecordRepository.save(existingRecord);
        });
    }
    
    /**
     * Delete a medical record
     * @param recordId The record ID
     */
    public void deleteMedicalRecord(String recordId) {
        log.info("Deleting medical record with ID: {}", recordId);
        medicalRecordRepository.deleteById(recordId);
    }
    
    /**
     * Get all medical records
     * @return List of all medical records
     */
    public List<MedicalRecord> getAllMedicalRecords() {
        log.info("Fetching all medical records");
        return medicalRecordRepository.findAll();
    }
}
