package com.example.health_care_system.repository;

import com.example.health_care_system.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    
    List<Payment> findByPatientId(String patientId);
    
    Optional<Payment> findByAppointmentId(String appointmentId);
    
    List<Payment> findByHospitalId(String hospitalId);
    
    List<Payment> findByDoctorId(String doctorId);
    
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByPatientIdAndStatus(String patientId, Payment.PaymentStatus status);
}
