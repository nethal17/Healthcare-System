package com.example.health_care_system.service;

import com.example.health_care_system.exception.ResourceNotFoundException;
import com.example.health_care_system.factory.PaymentStrategyFactory;
import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Payment;
import com.example.health_care_system.repository.PaymentRepository;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.strategy.PaymentContext;
import com.example.health_care_system.strategy.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private AppointmentRepository appointmentRepository;
    private PaymentStrategyFactory paymentStrategyFactory;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        appointmentRepository = mock(AppointmentRepository.class);
        paymentStrategyFactory = mock(PaymentStrategyFactory.class);

        paymentService = new PaymentService(paymentRepository, appointmentRepository, paymentStrategyFactory);
    }

    @Test
    void createPayment_success_callsStrategyAndReturnsPayment() {
        String appointmentId = "apt-1";
        Payment.PaymentMethod method = Payment.PaymentMethod.CARD;
        BigDecimal amount = new BigDecimal("123.45");

        Appointment apt = new Appointment();
        apt.setId(appointmentId);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(apt));

        PaymentStrategy strategy = mock(PaymentStrategy.class);
        when(paymentStrategyFactory.getStrategy(method)).thenReturn(strategy);

        Payment created = new Payment();
        created.setId("pay-1");
        created.setAppointmentId(appointmentId);
        created.setAmount(amount);
        created.setPaymentMethod(method);
        created.setStatus(Payment.PaymentStatus.COMPLETED);

        when(strategy.createPayment(eq(appointmentId), eq(amount), any(PaymentContext.class))).thenReturn(created);

        Payment result = paymentService.createPayment(appointmentId, method, amount, new PaymentContext());

        assertNotNull(result);
        assertEquals("pay-1", result.getId());
        assertEquals(appointmentId, result.getAppointmentId());
        assertEquals(amount, result.getAmount());

        verify(appointmentRepository).findById(appointmentId);
        verify(paymentStrategyFactory).getStrategy(method);
        verify(strategy).createPayment(eq(appointmentId), eq(amount), any(PaymentContext.class));
    }

    @Test
    void createPayment_appointmentMissing_throwsResourceNotFoundException() {
        String appointmentId = "missing";
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            paymentService.createPayment(appointmentId, Payment.PaymentMethod.CASH, BigDecimal.ZERO, new PaymentContext())
        );

        verify(appointmentRepository).findById(appointmentId);
        verifyNoInteractions(paymentStrategyFactory);
    }

    @Test
    void getPaymentByAppointmentId_delegatesToRepository() {
        String appointmentId = "apt-2";
        Payment p = new Payment(); p.setId("p2"); p.setAppointmentId(appointmentId);
        when(paymentRepository.findByAppointmentId(appointmentId)).thenReturn(Optional.of(p));

        Optional<Payment> res = paymentService.getPaymentByAppointmentId(appointmentId);
        assertTrue(res.isPresent());
        assertEquals("p2", res.get().getId());

        verify(paymentRepository).findByAppointmentId(appointmentId);
    }

    @Test
    void getPaymentsByPatientId_delegatesToRepository() {
        String patientId = "patient-1";
        Payment p = new Payment(); p.setId("p3"); p.setPatientId(patientId);
        when(paymentRepository.findByPatientId(patientId)).thenReturn(List.of(p));

        List<Payment> res = paymentService.getPaymentsByPatientId(patientId);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals("p3", res.get(0).getId());

        verify(paymentRepository).findByPatientId(patientId);
    }

    @Test
    void getPaymentByTransactionId_delegatesToRepository() {
        String txn = "txn-1";
        Payment p = new Payment(); p.setId("p4"); p.setTransactionId(txn);
        when(paymentRepository.findByTransactionId(txn)).thenReturn(Optional.of(p));

        Optional<Payment> res = paymentService.getPaymentByTransactionId(txn);
        assertTrue(res.isPresent());
        assertEquals("p4", res.get().getId());

        verify(paymentRepository).findByTransactionId(txn);
    }

    @Test
    void updatePaymentStatus_success_updatesAndSaves() {
        String paymentId = "pay-2";
        Payment existing = new Payment();
        existing.setId(paymentId);
        existing.setStatus(Payment.PaymentStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existing));

        Payment saved = new Payment(); saved.setId(paymentId); saved.setStatus(Payment.PaymentStatus.COMPLETED);
        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

        Payment res = paymentService.updatePaymentStatus(paymentId, Payment.PaymentStatus.COMPLETED);
        assertNotNull(res);
        assertEquals(Payment.PaymentStatus.COMPLETED, res.getStatus());

        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_missing_throwsResourceNotFoundException() {
        when(paymentRepository.findById("nope")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.updatePaymentStatus("nope", Payment.PaymentStatus.REFUNDED));
        verify(paymentRepository).findById("nope");
    }

    @Test
    void createPayment_nullStrategy_throws() {
        String appointmentId = "apt-9";
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(new Appointment()));
        when(paymentStrategyFactory.getStrategy(any())).thenReturn(null);

        assertThrows(NullPointerException.class, () -> paymentService.createPayment(appointmentId, Payment.PaymentMethod.CARD, new java.math.BigDecimal("10"), new PaymentContext()));
    }

    @Test
    void createPayment_nullAmount_throws() {
        String appointmentId = "apt-10";
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(new Appointment()));
        PaymentStrategy strategy = mock(PaymentStrategy.class);
        when(paymentStrategyFactory.getStrategy(any())).thenReturn(strategy);

        assertThrows(NullPointerException.class, () -> paymentService.createPayment(appointmentId, Payment.PaymentMethod.CASH, null, new PaymentContext()));
    }

    @Test
    void getPaymentsByHospitalAndDoctor_delegatesToRepository() {
        String hid = "h-1";
        String did = "d-1";
        Payment ph = new Payment(); ph.setId("ph");
        Payment pd = new Payment(); pd.setId("pd");
        when(paymentRepository.findByHospitalId(hid)).thenReturn(List.of(ph));
        when(paymentRepository.findByDoctorId(did)).thenReturn(List.of(pd));

        List<Payment> resH = paymentService.getPaymentsByHospitalId(hid);
        List<Payment> resD = paymentService.getPaymentsByDoctorId(did);

        assertEquals(1, resH.size());
        assertEquals("ph", resH.get(0).getId());
        assertEquals(1, resD.size());
        assertEquals("pd", resD.get(0).getId());

        verify(paymentRepository).findByHospitalId(hid);
        verify(paymentRepository).findByDoctorId(did);
    }
}

