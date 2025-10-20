package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.*;
import com.example.health_care_system.repository.*;
import com.example.health_care_system.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentControllerUnitTest {

    @Mock AppointmentService appointmentService;
    @Mock AppointmentRepository appointmentRepository;
    @Mock PatientRepository patientRepository;
    @Mock DoctorRepository doctorRepository;
    @Mock HospitalRepository hospitalRepository;
    @Mock PaymentService paymentService;
    @Mock PdfGenerationService pdfGenerationService;
    @Mock EmailService emailService;
    @Mock Model model;
    @Mock HttpSession session;

    PaymentController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PaymentController(
                appointmentService,
                appointmentRepository,
                patientRepository,
                doctorRepository,
                hospitalRepository,
                paymentService,
                pdfGenerationService,
                emailService
        );
    }

    @Test
    void index_returnsIndex() {
        assertEquals("index", controller.index());
    }

    @Test
    void insuranceCollection_redirectsWhenNoUser() {
        when(session.getAttribute("user")).thenReturn(null);
        String view = controller.InsuranceCollection(model, session);
        assertEquals("redirect:/login", view);
    }

    @Test
    void downloadInsuranceConfirmation_notFoundWhenNoSessionUser() {
        when(session.getAttribute("user")).thenReturn(null);
        ResponseEntity<byte[]> res = controller.downloadInsuranceConfirmation("a1", session);
        assertEquals(401, res.getStatusCodeValue());
    }

    @Test
    void downloadInsuranceConfirmation_notFoundForMissingAppointment() {
        UserDTO user = new UserDTO(); user.setId("p1");
        when(session.getAttribute("user")).thenReturn(user);
        when(appointmentRepository.findById("a1")).thenReturn(Optional.empty());

        ResponseEntity<byte[]> res = controller.downloadInsuranceConfirmation("a1", session);
        assertEquals(404, res.getStatusCodeValue());
    }

    @Test
    void downloadInsuranceConfirmation_success_returnsPdf() throws Exception {
        UserDTO user = new UserDTO(); user.setId("p1");
        when(session.getAttribute("user")).thenReturn(user);

        Appointment apt = new Appointment(); apt.setId("a1"); apt.setPatientId("p1"); apt.setAppointmentDateTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(9,0)));
        when(appointmentRepository.findById("a1")).thenReturn(Optional.of(apt));

        Patient p = new Patient(); p.setId("p1"); p.setName("P"); when(patientRepository.findById("p1")).thenReturn(Optional.of(p));

        when(doctorRepository.findById(any())).thenReturn(Optional.empty());
        when(hospitalRepository.findById(any())).thenReturn(Optional.empty());

    when(pdfGenerationService.generateInsuranceAppointmentPdf(any(), any(), any(), any(), any(), any())).thenReturn(new byte[]{1,2,3});

        ResponseEntity<byte[]> res = controller.downloadInsuranceConfirmation("a1", session);
        assertEquals(200, res.getStatusCodeValue());
        assertArrayEquals(new byte[]{1,2,3}, res.getBody());
    }
}
