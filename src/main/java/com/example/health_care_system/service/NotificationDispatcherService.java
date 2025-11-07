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
    private final NotificationService whatsappService;
    private final NotificationService smsService;

    public NotificationDispatcherService(AppointmentRepository appointmentRepository,
                                         NotificationHistoryRepository historyRepository,
                                         PatientRepository patientRepository,
                                         @Qualifier("whatsappNotificationService") NotificationService whatsappService,
                                         @Qualifier("smsNotificationService") NotificationService smsService) {
        this.appointmentRepository = appointmentRepository;
        this.historyRepository = historyRepository;
        this.patientRepository = patientRepository;
        this.whatsappService = whatsappService;
        this.smsService = smsService;
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

        // New behavior: prefer sending a single SMS to all recipients in one call when possible
        // Collect phone numbers for all appointments
        List<String> recipients = toNotify.stream()
                .map(appt -> patientRepository.findById(appt.getPatientId()).map(p -> p.getContactNumber()).orElse(null))
                .filter(num -> num != null && !num.isBlank())
                .map(num -> num.replaceAll("\\+", ""))
                .distinct()
                .collect(Collectors.toList());

        if (!recipients.isEmpty()) {
            String combined = String.join(",", recipients);
            // Use a generic message for the mass-SMS (cannot personalize in a single send)
            String genericMessage = String.format("Dr. %s has arrived for your appointment today. Please proceed to the clinic.",
                    (toNotify.get(0).getDoctorName() == null ? "doctor" : toNotify.get(0).getDoctorName()) );

            NotificationRequest bulkReq = new NotificationRequest(
                    null,
                    null,
                    doctorId,
                    null,
                    combined,
                    null,
                    genericMessage
            );

            NotificationResult bulkResult = smsService.send(bulkReq);
            if (bulkResult.getStatus() == NotificationResult.Status.SENT) {
                // mark each appointment as SMS SENT
                toNotify.forEach(appt -> saveHistory(appt, "SMS", "SENT", genericMessage, null));
            } else {
                // fallback: try per-appointment WhatsApp (as before)
                for (Appointment appt : toNotify) {
                    try {
                        String message = buildMessage(appt);
                        String phone = null;
                        if (appt.getPatientId() != null) {
                            phone = patientRepository.findById(appt.getPatientId())
                                    .map(p -> p.getContactNumber())
                                    .orElse(null);
                        }

                        NotificationRequest req = new NotificationRequest(
                                appt.getId(),
                                appt.getPatientId(),
                                appt.getDoctorId(),
                                appt.getPatientName(),
                                phone,
                                phone,
                                message
                        );

                        NotificationResult waResult = whatsappService.send(req);
                        if (waResult.getStatus() == NotificationResult.Status.SENT) {
                            saveHistory(appt, "WHATSAPP", "SENT", message, null);
                        } else {
                            saveHistory(appt, "SMS+WHATSAPP", "FAILED", message, (bulkResult.getError() == null ? "" : bulkResult.getError()) + " | " + (waResult.getError() == null ? "" : waResult.getError()));
                        }
                    } catch (Exception ex) {
                        saveHistory(appt, "SYSTEM", "FAILED", null, ex.getMessage());
                    }
                }
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
