package com.example.health_care_system.service;

import com.example.health_care_system.model.Payment;
import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.repository.PaymentRepository;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.HospitalRepository;
import com.example.health_care_system.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private HospitalRepository hospitalRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    /**
     * Create a payment record for card payment after successful Stripe transaction
     * 
     * @param appointmentId The appointment ID
     * @param transactionId The Stripe transaction/session ID
     * @param amount The payment amount
     * @return The created Payment object
     */
    public Payment createCardPayment(String appointmentId, String transactionId, BigDecimal amount) {
        // Get appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Get patient
        Patient patient = patientRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        // Get doctor
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        // Get hospital
        Hospital hospital = hospitalRepository.findById(doctor.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        // Create payment record
        Payment payment = new Payment();
        payment.setAppointmentId(appointmentId);
        
        // User/Patient Information
        payment.setPatientId(patient.getId());
        payment.setPatientName(patient.getName());
        
        // Hospital Information
        payment.setHospitalId(hospital.getId());
        payment.setHospitalName(hospital.getName());
        
        // Doctor Information
        payment.setDoctorId(doctor.getId());
        payment.setDoctorName(doctor.getName());
        payment.setDoctorSpecialization(doctor.getSpecialization());
        
        // Payment Information
        payment.setAmount(amount);
        payment.setPaymentMethod(Payment.PaymentMethod.CARD);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId(transactionId);
        
        // Timestamps
        LocalDateTime now = LocalDateTime.now();
        payment.setPaymentDate(now);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        
        // Save and return
        return paymentRepository.save(payment);
    }
    
    /**
     * Create a payment record for cash payment
     */
    public Payment createCashPayment(String appointmentId, BigDecimal amount) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        Patient patient = patientRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        Hospital hospital = hospitalRepository.findById(doctor.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        Payment payment = new Payment();
        payment.setAppointmentId(appointmentId);
        payment.setPatientId(patient.getId());
        payment.setPatientName(patient.getName());
        payment.setHospitalId(hospital.getId());
        payment.setHospitalName(hospital.getName());
        payment.setDoctorId(doctor.getId());
        payment.setDoctorName(doctor.getName());
        payment.setDoctorSpecialization(doctor.getSpecialization());
        payment.setAmount(amount);
        payment.setPaymentMethod(Payment.PaymentMethod.CASH);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        
        LocalDateTime now = LocalDateTime.now();
        payment.setPaymentDate(now);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        
        return paymentRepository.save(payment);
    }
    
    /**
     * Create a payment record for insurance payment
     */
    public Payment createInsurancePayment(String appointmentId, BigDecimal amount, 
                                          String insuranceProvider, String policyNumber) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        Patient patient = patientRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        Hospital hospital = hospitalRepository.findById(doctor.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        Payment payment = new Payment();
        payment.setAppointmentId(appointmentId);
        payment.setPatientId(patient.getId());
        payment.setPatientName(patient.getName());
        payment.setHospitalId(hospital.getId());
        payment.setHospitalName(hospital.getName());
        payment.setDoctorId(doctor.getId());
        payment.setDoctorName(doctor.getName());
        payment.setDoctorSpecialization(doctor.getSpecialization());
        payment.setAmount(amount);
        payment.setPaymentMethod(Payment.PaymentMethod.INSURANCE);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setInsuranceProvider(insuranceProvider);
        payment.setInsurancePolicyNumber(policyNumber);
        
        LocalDateTime now = LocalDateTime.now();
        payment.setPaymentDate(now);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        
        return paymentRepository.save(payment);
    }
    
    /**
     * Get payment by appointment ID
     */
    public Optional<Payment> getPaymentByAppointmentId(String appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId);
    }
    
    /**
     * Get all payments for a patient
     */
    public List<Payment> getPaymentsByPatientId(String patientId) {
        return paymentRepository.findByPatientId(patientId);
    }
    
    /**
     * Get payment by transaction ID
     */
    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }
    
    /**
     * Update payment status
     */
    public Payment updatePaymentStatus(String paymentId, Payment.PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }
    
    /**
     * Get all payments by hospital
     */
    public List<Payment> getPaymentsByHospitalId(String hospitalId) {
        return paymentRepository.findByHospitalId(hospitalId);
    }
    
    /**
     * Get all payments by doctor
     */
    public List<Payment> getPaymentsByDoctorId(String doctorId) {
        return paymentRepository.findByDoctorId(doctorId);
    }
}
