package com.example.health_care_system.service;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.NotificationHistory;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.repository.NotificationHistoryRepository;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.service.notification.NotificationRequest;
import com.example.health_care_system.service.notification.NotificationResult;
import com.example.health_care_system.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationDispatcherService {

    private final AppointmentRepository appointmentRepository;
    private final NotificationHistoryRepository historyRepository;
    private final PatientRepository patientRepository;
    private final NotificationService emailService;

    public NotificationDispatcherService(AppointmentRepository appointmentRepository,
                                         NotificationHistoryRepository historyRepository,
                                         PatientRepository patientRepository,
                                         @Qualifier("emailNotificationService") NotificationService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.historyRepository = historyRepository;
        this.patientRepository = patientRepository;
        this.emailService = emailService;
    }

    @Async
    public void notifyDoctorArrived(String doctorId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        List<Appointment> todays = appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(doctorId, start, end);

        // filter confirmed
        List<Appointment> toNotify = todays.stream()
                .filter(a -> a.getStatus() != null && a.getStatus().name().equals("CONFIRMED"))
                .collect(Collectors.toList());

        for (Appointment appt : toNotify) {
            try {
                String email = appt.getPatientId() == null ? null : patientRepository.findById(appt.getPatientId()).map(p -> p.getEmail()).orElse(null);
                String message = buildMessage(appt);
                if (email == null || email.isBlank()) {
                    // record that there's no email to send
                    saveHistory(appt, "EMAIL", "FAILED", message, "no-email");
                    continue;
                }

                NotificationRequest emailReq = new NotificationRequest(
                        appt.getId(),
                        appt.getPatientId(),
                        appt.getDoctorId(),
                        appt.getPatientName(),
                        null,
                        null,
                        email,
                        message
                );

                NotificationResult emailRes = null;
                try {
                    emailRes = emailService.send(emailReq);
                } catch (Exception ex) {
                    // will be handled below as failure
                }

                if (emailRes != null && emailRes.getStatus() == NotificationResult.Status.SENT) {
                    saveHistory(appt, "EMAIL", "SENT", message, null);
                } else {
                    saveHistory(appt, "EMAIL", "FAILED", message, emailRes == null ? "send-failed" : emailRes.getError());
                }

            } catch (Exception ex) {
                saveHistory(appt, "SYSTEM", "FAILED", null, ex.getMessage());
            }
        }
    }

    private String buildMessage(Appointment appt) {
        return String.format("Dear %s, Dr. %s has arrived for your appointment at %s. Please proceed to the clinic.",
                appt.getPatientName() == null ? "patient" : appt.getPatientName(),
                appt.getDoctorName() == null ? "doctor" : appt.getDoctorName(),
                appt.getAppointmentDateTime() == null ? "today" : appt.getAppointmentDateTime().toLocalTime().toString()
        );
    }

    private void saveHistory(Appointment appt, String channel, String status, String message, String error) {
        NotificationHistory h = new NotificationHistory();
        h.setId(UUID.randomUUID().toString());
        h.setAppointmentId(appt.getId());
        h.setPatientId(appt.getPatientId());
        h.setDoctorId(appt.getDoctorId());
        h.setChannel(channel);
        h.setStatus(status);
        h.setSentAt(Instant.now());
        h.setMessage(message);
        h.setError(error);
        historyRepository.save(h);
    }
}
