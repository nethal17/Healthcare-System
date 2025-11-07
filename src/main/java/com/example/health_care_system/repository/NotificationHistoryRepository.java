package com.example.health_care_system.repository;

import com.example.health_care_system.model.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, String> {
    List<NotificationHistory> findByPatientId(String patientId);
    List<NotificationHistory> findByDoctorId(String doctorId);
    List<NotificationHistory> findByAppointmentId(String appointmentId);
    List<NotificationHistory> findByChannel(String channel);
}
