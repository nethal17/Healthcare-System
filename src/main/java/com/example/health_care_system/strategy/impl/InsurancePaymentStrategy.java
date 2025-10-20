package com.example.health_care_system.strategy.impl;

import com.example.health_care_system.exception.ResourceNotFoundException;
import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Payment;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.HospitalRepository;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.repository.PaymentRepository;
import com.example.health_care_system.strategy.PaymentContext;
import com.example.health_care_system.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Strategy implementation for insurance payments.
 * Handles insurance-based payment processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InsurancePaymentStrategy implements PaymentStrategy {
    
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final PaymentRepository paymentRepository;
    
    @Override
    public Payment createPayment(String appointmentId, BigDecimal amount, PaymentContext context) {
        log.info("Creating insurance payment for appointment: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        
        Patient patient = patientRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", appointment.getPatientId()));
        
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", appointment.getDoctorId()));
        
        Hospital hospital = hospitalRepository.findById(doctor.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", doctor.getHospitalId()));
        
        Payment payment = buildPayment(appointment, patient, doctor, hospital, amount, context);
        payment.setPaymentMethod(Payment.PaymentMethod.INSURANCE);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setInsuranceProvider(context.getInsuranceProvider());
        payment.setInsurancePolicyNumber(context.getPolicyNumber());
        
        return paymentRepository.save(payment);
    }
    
    @Override
    public Payment.PaymentMethod getPaymentMethod() {
        return Payment.PaymentMethod.INSURANCE;
    }
    
    private Payment buildPayment(Appointment appointment, Patient patient, 
                                 Doctor doctor, Hospital hospital, 
                                 BigDecimal amount, PaymentContext context) {
        Payment payment = new Payment();
        payment.setAppointmentId(appointment.getId());
        payment.setPatientId(patient.getId());
        payment.setPatientName(patient.getName());
        payment.setHospitalId(hospital.getId());
        payment.setHospitalName(hospital.getName());
        payment.setDoctorId(doctor.getId());
        payment.setDoctorName(doctor.getName());
        payment.setDoctorSpecialization(doctor.getSpecialization());
        payment.setAmount(amount);
        
        LocalDateTime now = LocalDateTime.now();
        payment.setPaymentDate(now);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        
        return payment;
    }
}
