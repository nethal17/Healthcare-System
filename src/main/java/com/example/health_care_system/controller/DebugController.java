package com.example.health_care_system.controller;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);
    private final AppointmentRepository appointmentRepository;

    public DebugController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/doctors/{id}/appointments/today")
    public ResponseEntity<?> getTodaysAppointments(@PathVariable("id") String id) {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.atTime(LocalTime.MAX);
            List<Appointment> appts = appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(id, start, end);
            List<Appointment> confirmed = appts.stream()
                    .filter(a -> a.getStatus() != null && a.getStatus().name().equals("CONFIRMED"))
                    .toList();
            return ResponseEntity.ok(confirmed);
        } catch (Exception ex) {
            logger.error("Error fetching today's appointments for doctor {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(500).body("error");
        }
    }

    @GetMapping("/doctors/{id}/appointments/today/all")
    public ResponseEntity<?> getTodaysAppointmentsAll(@PathVariable("id") String id) {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.atTime(LocalTime.MAX);
            List<Appointment> appts = appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(id, start, end);
            return ResponseEntity.ok(appts);
        } catch (Exception ex) {
            logger.error("Error fetching today's appointments for doctor {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(500).body("error");
        }
    }

    @PostMapping("/appointments/{appointmentId}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable("appointmentId") String appointmentId) {
        try {
            return appointmentRepository.findById(appointmentId)
                    .map(appt -> {
                        appt.setStatus(Appointment.AppointmentStatus.CONFIRMED);
                        appt.setUpdatedAt(LocalDateTime.now());
                        appointmentRepository.save(appt);
                        return ResponseEntity.ok(Map.of("status", "confirmed", "id", appointmentId));
                    })
                    .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "not-found")));
        } catch (Exception ex) {
            logger.error("Error confirming appointment {}: {}", appointmentId, ex.getMessage(), ex);
            return ResponseEntity.status(500).body("error");
        }
    }

    @PostMapping("/appointments/{appointmentId}/complete")
    public ResponseEntity<?> completeAppointment(@PathVariable("appointmentId") String appointmentId) {
        try {
            return appointmentRepository.findById(appointmentId)
                    .map(appt -> {
                        appt.setStatus(Appointment.AppointmentStatus.COMPLETED);
                        appt.setUpdatedAt(LocalDateTime.now());
                        appt.setActualCheckOutTime(LocalDateTime.now());
                        appointmentRepository.save(appt);
                        return ResponseEntity.ok(Map.of("status", "completed", "id", appointmentId));
                    })
                    .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "not-found")));
        } catch (Exception ex) {
            logger.error("Error completing appointment {}: {}", appointmentId, ex.getMessage(), ex);
            return ResponseEntity.status(500).body("error");
        }
    }
}
