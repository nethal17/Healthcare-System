package com.example.health_care_system.service;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.NotificationHistory;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.repository.NotificationHistoryRepository;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.service.notification.NotificationRequest;
import com.example.health_care_system.service.notification.NotificationResult;
import com.example.health_care_system.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationDispatcherServiceTest {

    @Mock
    AppointmentRepository appointmentRepository;

    @Mock
    NotificationHistoryRepository historyRepository;

    @Mock
    PatientRepository patientRepository;

    @Mock
    NotificationService emailNotificationService;

    NotificationDispatcherService dispatcherService;

    private Appointment appointment;
    private Patient patient;

    @BeforeEach
    void setUp() {
        appointment = new Appointment();
        appointment.setId("a1");
        appointment.setDoctorId("d1");
        appointment.setDoctorName("Dr Strange");
        appointment.setPatientId("p1");
        appointment.setPatientName("John Doe");
        appointment.setAppointmentDateTime(LocalDateTime.now().plusHours(1));
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);

        patient = new Patient();
        patient.setId("p1");
        patient.setContactNumber("+94770000000");
        patient.setEmail("john@example.com");

        // Explicitly construct the service with email-only
        dispatcherService = new NotificationDispatcherService(
                appointmentRepository,
                historyRepository,
                patientRepository,
                emailNotificationService
        );
    }

    @Test
    void testEmailSent_whenPatientHasEmail_historySavedAsSent() {
        // Arrange
        when(appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(eq("d1"), any(), any()))
                .thenReturn(List.of(appointment));
        when(patientRepository.findById("p1")).thenReturn(Optional.of(patient));

        when(emailNotificationService.send(any(NotificationRequest.class)))
                .thenReturn(new NotificationResult(NotificationResult.Status.SENT, "sent", null));

        List<NotificationHistory> saved = new ArrayList<>();
        when(historyRepository.save(any(NotificationHistory.class))).thenAnswer(invocation -> {
            NotificationHistory h = invocation.getArgument(0);
            saved.add(h);
            return h;
        });

        // Act
        dispatcherService.notifyDoctorArrived("d1");

        // Assert
        assertThat(saved).isNotEmpty();
        assertThat(saved.stream().filter(h -> "EMAIL".equals(h.getChannel())).map(NotificationHistory::getStatus)).contains("SENT");
    }

    @Test
    void testNoEmail_whenPatientHasNoEmail_historyRecordedAsFailedNoEmail() {
        // Arrange: patient without email
        patient.setEmail(null);
        when(appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(eq("d1"), any(), any()))
                .thenReturn(List.of(appointment));
        when(patientRepository.findById("p1")).thenReturn(Optional.of(patient));

        List<NotificationHistory> saved = new ArrayList<>();
        when(historyRepository.save(any(NotificationHistory.class))).thenAnswer(invocation -> {
            NotificationHistory h = invocation.getArgument(0);
            saved.add(h);
            return h;
        });

        // Act
        dispatcherService.notifyDoctorArrived("d1");

        // Assert: expect EMAIL entry with FAILED and error "no-email"
        assertThat(saved).isNotEmpty();
        assertThat(saved.stream().filter(h -> "EMAIL".equals(h.getChannel())).map(NotificationHistory::getError)).contains("no-email");
    }
}
