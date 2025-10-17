package com.example.health_care_system.service;

import com.example.health_care_system.model.MedicalRecord;
import com.example.health_care_system.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    private MedicalRecordService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MedicalRecordService(medicalRecordRepository);
    }

    @Test
    void getPatientMedicalRecords_delegates() {
        when(medicalRecordRepository.findByPatientIdOrderByRecordDateDesc("p1"))
            .thenReturn(List.of(new MedicalRecord()));
        var list = service.getPatientMedicalRecords("p1");
        assertEquals(1, list.size());
    }

    @Test
    void create_update_delete_and_getAll() {
        MedicalRecord rec = new MedicalRecord();
        rec.setPatientName("P");
        when(medicalRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MedicalRecord created = service.createMedicalRecord(rec);
        assertNotNull(created.getCreatedAt());

        MedicalRecord existing = new MedicalRecord(); existing.setId("r1"); existing.setDiagnosis("old");
        when(medicalRecordRepository.findById("r1")).thenReturn(Optional.of(existing));
        when(medicalRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MedicalRecord updated = new MedicalRecord(); updated.setDiagnosis("new"); updated.setPrescription("pres"); updated.setNotes("n");
        var opt = service.updateMedicalRecord("r1", updated);
        assertTrue(opt.isPresent());
        assertEquals("new", opt.get().getDiagnosis());

        service.deleteMedicalRecord("r1");
        verify(medicalRecordRepository).deleteById("r1");

        when(medicalRecordRepository.findAll()).thenReturn(List.of(created));
        assertEquals(1, service.getAllMedicalRecords().size());
    }
}

