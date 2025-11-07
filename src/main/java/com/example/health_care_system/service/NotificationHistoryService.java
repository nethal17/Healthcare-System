package com.example.health_care_system.service;

import com.example.health_care_system.model.NotificationHistory;
import com.example.health_care_system.repository.NotificationHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationHistoryService {

    private final NotificationHistoryRepository repository;

    public NotificationHistoryService(NotificationHistoryRepository repository) {
        this.repository = repository;
    }

    public Optional<NotificationHistory> getById(String id) {
        return repository.findById(id);
    }

    public List<NotificationHistory> getAll() {
        return repository.findAll();
    }

    public List<NotificationHistory> getByPatientId(String patientId) {
        return repository.findByPatientId(patientId);
    }

    public List<NotificationHistory> getByDoctorId(String doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    public List<NotificationHistory> getByAppointmentId(String appointmentId) {
        return repository.findByAppointmentId(appointmentId);
    }

    public List<NotificationHistory> getByChannel(String channel) {
        return repository.findByChannel(channel);
    }
}

