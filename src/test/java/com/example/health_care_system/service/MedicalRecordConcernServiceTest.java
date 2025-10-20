package com.example.health_care_system.service;

import com.example.health_care_system.model.MedicalRecordConcern;
import com.example.health_care_system.repository.MedicalRecordConcernRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicalRecordConcernServiceTest {

    @Mock
    private MedicalRecordConcernRepository concernRepository;

    private MedicalRecordConcernService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MedicalRecordConcernService(concernRepository);
    }

    @Test
    void createConcern_setsStatusAndSaves() {
        MedicalRecordConcern concern = new MedicalRecordConcern();
        concern.setPatientName("P");
        concern.setMedicalRecordId("r1");

        MedicalRecordConcern saved = new MedicalRecordConcern();
        saved.setId("c1");
        when(concernRepository.save(any())).thenReturn(saved);

        MedicalRecordConcern res = service.createConcern(concern);
        assertNotNull(res);
        assertEquals("c1", res.getId());
        assertEquals("P", concern.getPatientName());
        verify(concernRepository).save(any(MedicalRecordConcern.class));
    }

    @Test
    void getAllConcerns_and_filters() {
        MedicalRecordConcern c1 = new MedicalRecordConcern(); c1.setId("c1");
        when(concernRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(c1));
        assertEquals(1, service.getAllConcerns().size());

        when(concernRepository.findByPatientId("p1")).thenReturn(List.of(c1));
        assertEquals(1, service.getConcernsByPatientId("p1").size());

        when(concernRepository.findByStatus("PENDING")).thenReturn(List.of(c1));
        assertEquals(1, service.getConcernsByStatus("PENDING").size());

        when(concernRepository.findById("c1")).thenReturn(Optional.of(c1));
        assertTrue(service.getConcernById("c1").isPresent());
    }

    @Test
    void replyConcern_updatesAndSaves() {
        MedicalRecordConcern existing = new MedicalRecordConcern();
        existing.setId("c2");
        existing.setStatus("PENDING");
        when(concernRepository.findById("c2")).thenReturn(Optional.of(existing));
        when(concernRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MedicalRecordConcern result = service.replyConcern("c2", "Reply text", "doctor1");
        assertEquals("REPLIED", result.getStatus());
        assertEquals("Reply text", result.getReplyText());
        assertEquals("doctor1", result.getRepliedBy());
    }

    @Test
    void replyConcern_missing_throws() {
        when(concernRepository.findById("missing")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.replyConcern("missing", "r", "d"));
        assertTrue(ex.getMessage().contains("Concern not found"));
    }

    @Test
    void deleteConcern_delegates() {
        doNothing().when(concernRepository).deleteById("c3");
        service.deleteConcern("c3");
        verify(concernRepository).deleteById("c3");
    }
}

