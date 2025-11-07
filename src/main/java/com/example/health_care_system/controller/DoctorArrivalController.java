package com.example.health_care_system.controller;

import com.example.health_care_system.service.NotificationDispatcherService;
import com.example.health_care_system.service.NotificationHistoryService;
import com.example.health_care_system.service.AppointmentService;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.NotificationHistory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Qualifier;
import com.example.health_care_system.service.notification.NotificationService;
import com.example.health_care_system.service.notification.NotificationRequest;
import com.example.health_care_system.service.notification.NotificationResult;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.repository.NotificationHistoryRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doctors")
public class DoctorArrivalController {

    private final NotificationDispatcherService dispatcherService;
    private final NotificationHistoryService historyService;
    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService emailNotificationService;
    private final PatientRepository patientRepository;
    private final NotificationHistoryRepository historyRepository;
    private static final Logger logger = LoggerFactory.getLogger(DoctorArrivalController.class);

    public DoctorArrivalController(NotificationDispatcherService dispatcherService,
                                   NotificationHistoryService historyService,
                                   AppointmentService appointmentService,
                                   AppointmentRepository appointmentRepository,
                                   @Qualifier("emailNotificationService") NotificationService emailNotificationService,
                                   PatientRepository patientRepository,
                                   NotificationHistoryRepository historyRepository) {
        this.dispatcherService = dispatcherService;
        this.historyService = historyService;
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
        this.emailNotificationService = emailNotificationService;
        this.patientRepository = patientRepository;
        this.historyRepository = historyRepository;
    }

    @PatchMapping("/{id}/arrived")
    @PostMapping("/{id}/arrived")
    public ResponseEntity<?> doctorArrived(@PathVariable("id") String id,
                                           @RequestBody(required = false) java.util.Map<String, Object> payload) {
        logger.info("Doctor arrived endpoint invoked for id={} payload={}", id, payload);
        dispatcherService.notifyDoctorArrived(id);
        return ResponseEntity.accepted().body("Notifications queued");
    }

    // Simple POST endpoint that triggers email notifications only. This intentionally accepts no body
    // and is safe to call from a form/fetch without setting Content-Type to avoid Tomcat 400 in some setups.
    @PostMapping("/{id}/arrived/email")
    public ResponseEntity<?> doctorArrivedEmail(@PathVariable("id") String id) {
        logger.info("Doctor arrived (email-only) endpoint invoked for id={}", id);
        // For immediate email on button tap: send to today's SCHEDULED or CONFIRMED appointments
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDateTime start = today.atStartOfDay();
            java.time.LocalDateTime end = today.atTime(java.time.LocalTime.MAX);
            java.util.List<Appointment> appts = appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(id, start, end);
            java.util.List<Appointment> toNotify = appts.stream()
                    .filter(a -> a.getStatus() != null && (a.getStatus().name().equals("CONFIRMED") || a.getStatus().name().equals("SCHEDULED")))
                    .toList();

            for (Appointment appt : toNotify) {
                String email = appt.getPatientId() == null ? null : patientRepository.findById(appt.getPatientId()).map(p -> p.getEmail()).orElse(null);
                String message = String.format("Dear %s, Dr. %s has arrived for your appointment at %s. Please proceed to the clinic.",
                        appt.getPatientName() == null ? "patient" : appt.getPatientName(),
                        appt.getDoctorName() == null ? "doctor" : appt.getDoctorName(),
                        appt.getAppointmentDateTime() == null ? "today" : appt.getAppointmentDateTime().toLocalTime().toString()
                );

                if (email == null || email.isBlank()) {
                    NotificationHistory h = new NotificationHistory();
                    h.setId(UUID.randomUUID().toString());
                    h.setAppointmentId(appt.getId());
                    h.setPatientId(appt.getPatientId());
                    h.setDoctorId(appt.getDoctorId());
                    h.setChannel("EMAIL");
                    h.setStatus("FAILED");
                    h.setSentAt(Instant.now());
                    h.setMessage(message);
                    h.setError("no-email");
                    historyRepository.save(h);
                    continue;
                }

                NotificationRequest req = new NotificationRequest(
                        appt.getId(),
                        appt.getPatientId(),
                        appt.getDoctorId(),
                        appt.getPatientName(),
                        null,
                        null,
                        email,
                        message
                );

                NotificationResult res = emailNotificationService.send(req);

                NotificationHistory h = new NotificationHistory();
                h.setId(UUID.randomUUID().toString());
                h.setAppointmentId(appt.getId());
                h.setPatientId(appt.getPatientId());
                h.setDoctorId(appt.getDoctorId());
                h.setChannel("EMAIL");
                h.setStatus(res != null && res.getStatus() == NotificationResult.Status.SENT ? "SENT" : "FAILED");
                h.setSentAt(Instant.now());
                h.setMessage(message);
                h.setError(res == null ? "send-failed" : res.getError());
                historyRepository.save(h);
            }

            return ResponseEntity.accepted().body("Email notifications queued");
        } catch (Exception ex) {
            logger.error("Error sending email notifications for doctor {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(500).body("error");
        }
    }

    // Debug endpoint to log incoming request details (headers, method) to help diagnose 400 errors
    @GetMapping("/{id}/arrived/debug")
    public ResponseEntity<?> doctorArrivedDebug(@PathVariable("id") String id, HttpServletRequest request) {
        logger.info("Doctor arrived debug invoked for id={} method={} remoteAddr={}", id, request.getMethod(), request.getRemoteAddr());
        // Log headers
        java.util.Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            logger.info("Header: {}={}", name, request.getHeader(name));
        }
        return ResponseEntity.ok().body("debug-ok");
    }

    // Return recent notification history entries for this doctor (debug endpoint)
    @GetMapping("/{id}/notification-history")
    public ResponseEntity<?> getNotificationHistory(@PathVariable("id") String id,
                                                    @RequestParam(value = "limit", required = false) Integer limit) {
        try {
            java.util.List<NotificationHistory> rows = historyService.getByDoctorId(id);
            if (limit != null && limit > 0 && rows.size() > limit) {
                rows = rows.subList(0, limit);
            }
            return ResponseEntity.ok(rows);
        } catch (Exception ex) {
            logger.error("Error fetching notification history for doctor {}: {}", id, ex.getMessage());
            return ResponseEntity.status(500).body("error");
        }
    }

    // Return today's confirmed appointments for the doctor (debugging endpoint)
    @GetMapping("/{id}/appointments/today")
    public ResponseEntity<?> getTodaysAppointments(@PathVariable("id") String id) {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDateTime start = today.atStartOfDay();
            java.time.LocalDateTime end = today.atTime(java.time.LocalTime.MAX);
            java.util.List<Appointment> appts = appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(id, start, end);
            // Filter confirmed
            java.util.List<Appointment> confirmed = appts.stream()
                    .filter(a -> a.getStatus() != null && a.getStatus().name().equals("CONFIRMED"))
                    .toList();
            return ResponseEntity.ok(confirmed);
        } catch (Exception ex) {
            logger.error("Error fetching today's appointments for doctor {}: {}", id, ex.getMessage());
            return ResponseEntity.status(500).body("error");
        }
    }
}
