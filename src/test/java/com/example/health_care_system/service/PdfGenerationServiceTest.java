package com.example.health_care_system.service;

import com.example.health_care_system.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PdfGenerationServiceTest {

    private final PdfGenerationService service = new PdfGenerationService();

    @Test
    void generateAppointmentConfirmationPdf_returnsBytes() {
        Appointment appointment = new Appointment();
        appointment.setId("apt1");
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        Patient patient = new Patient();
        patient.setId("p1");
        patient.setName("John");
        patient.setEmail("j@example.com");

        Doctor doctor = new Doctor();
        doctor.setId("d1");
        doctor.setName("Dr X");
        doctor.setEmail("dr@example.com");
        doctor.setSpecialization("Cardio");

        Hospital hospital = new Hospital();
        hospital.setId("h1");
        hospital.setName("Hospital A");
        hospital.setType(Hospital.HospitalType.PRIVATE);
        hospital.setHospitalCharges(new BigDecimal("500"));

        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("500"));
        payment.setPaymentMethod(Payment.PaymentMethod.CARD);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId("txn123");

        byte[] pdf = service.generateAppointmentConfirmationPdf(appointment, patient, doctor, hospital, payment);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generateInsuranceAppointmentPdf_returnsBytes() {
        Appointment appointment = new Appointment();
        appointment.setId("apt2");
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(3));

        Patient patient = new Patient();
        patient.setId("p2");
        patient.setName("Alice");

        Doctor doctor = new Doctor();
        doctor.setId("d2");
        doctor.setName("Dr Y");

        Hospital hospital = new Hospital();
        hospital.setId("h2");
        hospital.setName("Hospital B");
        hospital.setType(Hospital.HospitalType.GOVERNMENT);

        byte[] pdf = service.generateInsuranceAppointmentPdf(appointment, patient, doctor, hospital, "InsureCo", "POL123");
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
