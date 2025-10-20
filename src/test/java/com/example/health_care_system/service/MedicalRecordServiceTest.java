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
import static org.mockito.ArgumentMatchers.any;
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
    void getPatientMedicalRecords_delegatesToRepository() {
        when(medicalRecordRepository.findByPatientIdOrderByRecordDateDesc("p1")).thenReturn(List.of(new MedicalRecord()));
        var list = service.getPatientMedicalRecords("p1");
        assertNotNull(list);
        assertEquals(1, list.size());
        verify(medicalRecordRepository).findByPatientIdOrderByRecordDateDesc("p1");
    }

    @Test
    void getDoctorMedicalRecords_delegatesToRepository() {
        when(medicalRecordRepository.findByDoctorId("d1")).thenReturn(List.of(new MedicalRecord()));
        var list = service.getDoctorMedicalRecords("d1");
        assertNotNull(list);
        assertEquals(1, list.size());
        verify(medicalRecordRepository).findByDoctorId("d1");
    }

    @Test
    void getMedicalRecordById_returnsOptional() {
        MedicalRecord rec = new MedicalRecord(); rec.setId("r1");
        when(medicalRecordRepository.findById("r1")).thenReturn(Optional.of(rec));
        var opt = service.getMedicalRecordById("r1");
        assertTrue(opt.isPresent());
        assertEquals("r1", opt.get().getId());
    }

    @Test
    void createMedicalRecord_setsTimestampsAndSaves() {
        MedicalRecord rec = new MedicalRecord(); rec.setPatientName("P");
        when(medicalRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MedicalRecord saved = service.createMedicalRecord(rec);
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        // createdAt should be very recent (within a few seconds)
        assertTrue(saved.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        verify(medicalRecordRepository).save(rec);
    }

    @Test
    void updateMedicalRecord_found_updatesAndReturns() {
        MedicalRecord existing = new MedicalRecord(); existing.setId("r2"); existing.setDiagnosis("old");
        MedicalRecord updated = new MedicalRecord(); updated.setDiagnosis("new"); updated.setPrescription("pres"); updated.setNotes("note");

        when(medicalRecordRepository.findById("r2")).thenReturn(Optional.of(existing));
        when(medicalRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var opt = service.updateMedicalRecord("r2", updated);
        assertTrue(opt.isPresent());
        var out = opt.get();
        assertEquals("new", out.getDiagnosis());
        assertEquals("pres", out.getPrescription());
        assertEquals("note", out.getNotes());
        verify(medicalRecordRepository).save(existing);
    }

    @Test
    void updateMedicalRecord_notFound_returnsEmpty() {
        when(medicalRecordRepository.findById("missing")).thenReturn(Optional.empty());
        var opt = service.updateMedicalRecord("missing", new MedicalRecord());
        assertTrue(opt.isEmpty());
    }

    @Test
    void createMedicalRecord_null_throws() {
        assertThrows(NullPointerException.class, () -> service.createMedicalRecord(null));
    }

    @Test
    void getMedicalRecordById_notFound_returnsEmpty() {
        when(medicalRecordRepository.findById("nope")).thenReturn(Optional.empty());
        var opt = service.getMedicalRecordById("nope");
        assertTrue(opt.isEmpty());
    }

    @Test
    void deleteMedicalRecord_delegates() {
        doNothing().when(medicalRecordRepository).deleteById("r3");
        service.deleteMedicalRecord("r3");
        verify(medicalRecordRepository).deleteById("r3");
    }

    @Test
    void getAllMedicalRecords_delegates() {
        when(medicalRecordRepository.findAll()).thenReturn(List.of(new MedicalRecord(), new MedicalRecord()));
        var list = service.getAllMedicalRecords();
        assertEquals(2, list.size());
        verify(medicalRecordRepository).findAll();
    }
}

