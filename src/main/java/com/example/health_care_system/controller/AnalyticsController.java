package com.example.health_care_system.controller;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @GetMapping("/patient-trends")
    public Map<String, Integer> getPatientTrends(
            @RequestParam String patientId,
            @RequestParam(defaultValue = "appointments") String type,
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (!"appointments".equals(type)) {
            throw new IllegalArgumentException("Currently only 'appointments' trend type is supported.");
        }
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        // Optionally filter by dates
        if (startDate != null) {
            appointments = appointments.stream()
                .filter(a -> a.getAppointmentDateTime() != null && !a.getAppointmentDateTime().toLocalDate().isBefore(startDate))
                .collect(Collectors.toList());
        }
        if (endDate != null) {
            appointments = appointments.stream()
                .filter(a -> a.getAppointmentDateTime() != null && !a.getAppointmentDateTime().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());
        }
        // Group by month (format: yyyy-MM)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Integer> monthlyCounts = new HashMap<>();
        for (Appointment apt : appointments) {
            if (apt.getAppointmentDateTime() != null) {
                String key = apt.getAppointmentDateTime().format(formatter);
                monthlyCounts.put(key, monthlyCounts.getOrDefault(key, 0) + 1);
            }
        }
        return monthlyCounts;
    }
}

