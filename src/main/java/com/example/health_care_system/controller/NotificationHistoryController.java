package com.example.health_care_system.controller;

import com.example.health_care_system.model.NotificationHistory;
import com.example.health_care_system.service.NotificationHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationHistoryController {

    private final NotificationHistoryService historyService;

    public NotificationHistoryController(NotificationHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<NotificationHistory>> getHistory(
            @RequestParam(value = "patientId", required = false) String patientId,
            @RequestParam(value = "doctorId", required = false) String doctorId,
            @RequestParam(value = "appointmentId", required = false) String appointmentId,
            @RequestParam(value = "channel", required = false) String channel
    ) {
        if (patientId != null && !patientId.isBlank()) {
            return ResponseEntity.ok(historyService.getByPatientId(patientId));
        }
        if (doctorId != null && !doctorId.isBlank()) {
            return ResponseEntity.ok(historyService.getByDoctorId(doctorId));
        }
        if (appointmentId != null && !appointmentId.isBlank()) {
            return ResponseEntity.ok(historyService.getByAppointmentId(appointmentId));
        }
        if (channel != null && !channel.isBlank()) {
            return ResponseEntity.ok(historyService.getByChannel(channel));
        }
        return ResponseEntity.ok(historyService.getAll());
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<NotificationHistory> getHistoryById(@PathVariable("id") String id) {
        return historyService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

