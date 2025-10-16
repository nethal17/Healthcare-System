package com.example.health_care_system.repository;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Appointment.AppointmentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    
    List<Appointment> findByPatientId(String patientId);
    
    List<Appointment> findByDoctorId(String doctorId);
    
    List<Appointment> findByStatus(AppointmentStatus status);
    
    List<Appointment> findByDoctorIdAndStatus(String doctorId, AppointmentStatus status);
    
    List<Appointment> findByPatientIdAndStatus(String patientId, AppointmentStatus status);
    
    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    List<Appointment> findByDoctorIdAndAppointmentDateTimeBetween(String doctorId, LocalDateTime start, LocalDateTime end);
}
