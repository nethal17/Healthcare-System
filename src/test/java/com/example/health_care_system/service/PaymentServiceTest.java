package com.example.health_care_system.service;

import com.example.health_care_system.model.*;
import com.example.health_care_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private HospitalRepository hospitalRepository;
    @Mock
    private PatientRepository patientRepository;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PaymentService();
        TestUtils.injectField(service, "paymentRepository", paymentRepository);
        TestUtils.injectField(service, "appointmentRepository", appointmentRepository);
        TestUtils.injectField(service, "doctorRepository", doctorRepository);
        TestUtils.injectField(service, "hospitalRepository", hospitalRepository);
        TestUtils.injectField(service, "patientRepository", patientRepository);
    }

    @Test
    void createCardPayment_success() {
        Appointment apt = new Appointment();
        apt.setId("a1");
        apt.setPatientId("p1");
        apt.setDoctorId("d1");
        when(appointmentRepository.findById("a1")).thenReturn(Optional.of(apt));

        Patient p = new Patient(); p.setId("p1"); p.setName("P");
        when(patientRepository.findById("p1")).thenReturn(Optional.of(p));

        Doctor d = new Doctor(); d.setId("d1"); d.setName("Dr"); d.setHospitalId("h1"); d.setSpecialization("S");
        when(doctorRepository.findById("d1")).thenReturn(Optional.of(d));

        Hospital h = new Hospital(); h.setId("h1"); h.setName("H");
        when(hospitalRepository.findById("h1")).thenReturn(Optional.of(h));

        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Payment payment = service.createCardPayment("a1", "txn1", new BigDecimal("1000"));
        assertEquals(Payment.PaymentMethod.CARD, payment.getPaymentMethod());
        assertEquals(Payment.PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals("txn1", payment.getTransactionId());
    }

    @Test
    void createCashPayment_and_insurance_and_queries() {
        Appointment apt = new Appointment(); apt.setId("a2"); apt.setPatientId("p2"); apt.setDoctorId("d2");
        when(appointmentRepository.findById("a2")).thenReturn(Optional.of(apt));
        Patient p = new Patient(); p.setId("p2"); p.setName("P2"); when(patientRepository.findById("p2")).thenReturn(Optional.of(p));
        Doctor d = new Doctor(); d.setId("d2"); d.setName("Dr2"); d.setHospitalId("h2"); when(doctorRepository.findById("d2")).thenReturn(Optional.of(d));
        Hospital h = new Hospital(); h.setId("h2"); h.setName("H2"); when(hospitalRepository.findById("h2")).thenReturn(Optional.of(h));

        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Payment cash = service.createCashPayment("a2", new BigDecimal("200"));
        assertEquals(Payment.PaymentMethod.CASH, cash.getPaymentMethod());
        assertEquals(Payment.PaymentStatus.PENDING, cash.getStatus());

        Payment ins = service.createInsurancePayment("a2", new BigDecimal("300"), "InsCo", "POL1");
        assertEquals(Payment.PaymentMethod.INSURANCE, ins.getPaymentMethod());
        assertEquals("InsCo", ins.getInsuranceProvider());

        when(paymentRepository.findByAppointmentId("a2")).thenReturn(Optional.of(ins));
        assertTrue(service.getPaymentByAppointmentId("a2").isPresent());

        when(paymentRepository.findByTransactionId("txnX")).thenReturn(Optional.empty());
        assertTrue(service.getPaymentByTransactionId("txnX").isEmpty());
    }

    @Test
    void updatePaymentStatus_and_queriesByEntity() {
        Payment p = new Payment(); p.setId("p1"); p.setStatus(Payment.PaymentStatus.PENDING);
        when(paymentRepository.findById("p1")).thenReturn(Optional.of(p));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Payment updated = service.updatePaymentStatus("p1", Payment.PaymentStatus.COMPLETED);
        assertEquals(Payment.PaymentStatus.COMPLETED, updated.getStatus());

        when(paymentRepository.findByHospitalId("h1")).thenReturn(java.util.List.of(updated));
        assertEquals(1, service.getPaymentsByHospitalId("h1").size());

        when(paymentRepository.findByDoctorId("d1")).thenReturn(java.util.List.of(updated));
        assertEquals(1, service.getPaymentsByDoctorId("d1").size());
    }
}

