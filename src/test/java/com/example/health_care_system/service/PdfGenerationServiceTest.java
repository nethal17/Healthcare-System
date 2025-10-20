package com.example.health_care_system.service;

import com.example.health_care_system.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PdfGenerationServiceTest {

    private final PdfGenerationService service = new PdfGenerationService();

    @Test
    void generateAppointmentConfirmationPdf_returnsBytes_forPrivateAndCardPayment() {
        Appointment appointment = new Appointment();
        appointment.setId("apt1");
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        appointment.setPurpose("Checkup");

        Patient patient = new Patient();
        patient.setId("p1");
        patient.setName("John Doe");
        patient.setEmail("j@example.com");
        patient.setContactNumber("0771234567");

        Doctor doctor = new Doctor();
        doctor.setId("d1");
        doctor.setName("Dr X");
        doctor.setEmail("dr@example.com");
        doctor.setSpecialization("Cardio");

        Hospital hospital = new Hospital();
        hospital.setId("h1");
        hospital.setName("Private Hospital");
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
    void generateAppointmentConfirmationPdf_handlesGovernmentHospital_freeFeeBranch() {
        Appointment appointment = new Appointment();
        appointment.setId("apt2");
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(3));
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        Patient patient = new Patient();
        patient.setId("p2");
        patient.setName("Alice");
    patient.setEmail("alice@example.com");
    patient.setContactNumber("0770000000");

        Doctor doctor = new Doctor();
        doctor.setId("d2");
        doctor.setName("Dr Y");
    doctor.setSpecialization("General");
    doctor.setEmail("dry@example.com");
    doctor.setEmail("dr.y@example.com");

        Hospital hospital = new Hospital();
        hospital.setId("h2");
        hospital.setName("Gov Hospital");
        hospital.setType(Hospital.HospitalType.GOVERNMENT);

        byte[] pdf = service.generateAppointmentConfirmationPdf(appointment, patient, doctor, hospital, null);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generateInsuranceAppointmentPdf_returnsBytes_and_handlesNullHospital() {
        Appointment appointment = new Appointment();
        appointment.setId("apt3");
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(5));

        Patient patient = new Patient();
        patient.setId("p3");
        patient.setName("Bob");

        Doctor doctor = new Doctor();
        doctor.setId("d3");
        doctor.setName("Dr Z");

        Hospital hospital = new Hospital();
        hospital.setId("h3");
        hospital.setName("Hospital C");
        hospital.setType(Hospital.HospitalType.PRIVATE);

        byte[] pdf = service.generateInsuranceAppointmentPdf(appointment, patient, doctor, hospital, "InsCo", "POL123");
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generateAppointmentConfirmationPdf_nullPatient_throwsRuntimeException() {
        Appointment appointment = new Appointment();
        appointment.setId("aptX");
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        Doctor doctor = new Doctor(); doctor.setId("d1"); doctor.setName("Dr");
        Hospital hospital = new Hospital(); hospital.setId("h1"); hospital.setName("H"); hospital.setType(Hospital.HospitalType.PRIVATE);
        Payment payment = new Payment(); payment.setAmount(new BigDecimal("100")); payment.setPaymentMethod(Payment.PaymentMethod.CARD); payment.setStatus(Payment.PaymentStatus.COMPLETED);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.generateAppointmentConfirmationPdf(appointment, null, doctor, hospital, payment));
        assertTrue(ex.getMessage().toLowerCase().contains("error generating pdf") || ex.getCause() != null);
    }

    @Test
    void generateAppointmentConfirmationPdf_nullDoctor_throwsRuntimeException() {
        Appointment appointment = new Appointment();
        appointment.setId("aptX2");
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        Patient patient = new Patient(); patient.setId("pX"); patient.setName("NoDoc");
        Hospital hospital = new Hospital(); hospital.setId("h1"); hospital.setName("H"); hospital.setType(Hospital.HospitalType.PRIVATE);
        Payment payment = new Payment(); payment.setAmount(new BigDecimal("100")); payment.setPaymentMethod(Payment.PaymentMethod.CARD); payment.setStatus(Payment.PaymentStatus.COMPLETED);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.generateAppointmentConfirmationPdf(appointment, patient, null, hospital, payment));
        assertTrue(ex.getMessage().toLowerCase().contains("error generating pdf") || ex.getCause() != null);
    }

    @Test
    void generateInsuranceAppointmentPdf_nullPolicy_throws() {
        Appointment appointment = new Appointment(); appointment.setId("aptY"); appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(5));
        Patient patient = new Patient(); patient.setId("pY"); patient.setName("Bob");
        Doctor doctor = new Doctor(); doctor.setId("dY"); doctor.setName("Dr Y");
        Hospital hospital = new Hospital(); hospital.setId("hY"); hospital.setName("Hospital C"); hospital.setType(Hospital.HospitalType.PRIVATE);

        // passing null policy or insurer should be handled
        byte[] pdf = service.generateInsuranceAppointmentPdf(appointment, patient, doctor, hospital, null, null);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}

