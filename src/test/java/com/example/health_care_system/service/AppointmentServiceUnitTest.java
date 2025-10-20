package com.example.health_care_system.service;

import com.example.health_care_system.config.AppointmentConfiguration;
import com.example.health_care_system.exception.BusinessLogicException;
import com.example.health_care_system.exception.ResourceNotFoundException;
import com.example.health_care_system.exception.ValidationException;
import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.TimeSlotReservation;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.repository.TimeSlotReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentServiceUnitTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private TimeSlotReservationRepository reservationRepository;
    @Mock
    private AppointmentConfiguration appointmentConfig;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AppointmentService(appointmentRepository, doctorRepository, patientRepository, reservationRepository, appointmentConfig);
        // sensible defaults for working hours
        when(appointmentConfig.getWorkingStart()).thenReturn(LocalTime.of(9,0));
        when(appointmentConfig.getWorkingEnd()).thenReturn(LocalTime.of(17,0));
        when(appointmentConfig.getSlotDurationMinutes()).thenReturn(30);
        when(appointmentConfig.getLunchStart()).thenReturn(LocalTime.of(13,0));
        when(appointmentConfig.getLunchEnd()).thenReturn(LocalTime.of(14,0));
        when(appointmentConfig.getMinimumAdvanceBookingHours()).thenReturn(0);
        when(appointmentConfig.getMaxDaysAhead()).thenReturn(7);
    }

    @Test
    void getAvailableTimeSlots_pastDate_throws() {
        LocalDate past = LocalDate.now().minusDays(1);
        assertThrows(ValidationException.class, () -> service.getAvailableTimeSlots("d1", past));
    }

    @Test
    void getAvailableTimeSlots_filtersBookedAndReserved() {
        LocalDate date = LocalDate.now().plusDays(2);
        // scheduled appointment at 9:00
        Appointment apt = new Appointment(); apt.setAppointmentDateTime(date.atTime(9,0)); apt.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(eq("d1"), any(), any())).thenReturn(List.of(apt));

        TimeSlotReservation res = new TimeSlotReservation(); res.setSlotDateTime(date.atTime(9,30)); res.setPatientId("other"); res.setStatus(TimeSlotReservation.ReservationStatus.ACTIVE);
        when(reservationRepository.findByDoctorIdAndStatus("d1", TimeSlotReservation.ReservationStatus.ACTIVE)).thenReturn(List.of(res));

        var slots = service.getAvailableTimeSlots("d1", date);
        assertFalse(slots.contains(LocalTime.of(9,0)));
        assertFalse(slots.contains(LocalTime.of(9,30)));
        assertTrue(slots.contains(LocalTime.of(10,0)));
    }

    @Test
    void bookAppointment_doctorNotFound_throws() {
        when(doctorRepository.findById("dX")).thenReturn(Optional.empty());
        LocalDateTime dt = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        assertThrows(ResourceNotFoundException.class, () -> service.bookAppointment("p","pname","dX", dt, null, null));
    }

    @Test
    void bookAppointment_slotAlreadyBooked_throws() {
        Doctor doctor = new Doctor(); doctor.setId("doc1"); doctor.setName("Dr");
        Patient patient = new Patient(); patient.setId("p1"); patient.setName("P");
        when(doctorRepository.findById("doc1")).thenReturn(Optional.of(doctor));
        when(patientRepository.findById("p1")).thenReturn(Optional.of(patient));

        LocalDateTime dt = LocalDate.now().plusDays(3).atTime(10,0);
        Appointment existing = new Appointment(); existing.setAppointmentDateTime(dt); existing.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(eq("doc1"), any(), any())).thenReturn(List.of(existing));

        assertThrows(BusinessLogicException.class, () -> service.bookAppointment("p1", "P", "doc1", dt, null, null));
    }

    @Test
    void bookAppointment_success() {
        Doctor doctor = new Doctor(); doctor.setId("doc2"); doctor.setName("Dr");
        Patient patient = new Patient(); patient.setId("p2"); patient.setName("P2");
        when(doctorRepository.findById("doc2")).thenReturn(Optional.of(doctor));
        when(patientRepository.findById("p2")).thenReturn(Optional.of(patient));

        LocalDateTime dt = LocalDate.now().plusDays(4).atTime(11,0);
        when(appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(eq("doc2"), any(), any())).thenReturn(List.of());
        when(reservationRepository.findByDoctorIdAndStatus(eq("doc2"), any())).thenReturn(List.of());

        Appointment saved = new Appointment(); saved.setId("a1");
        when(appointmentRepository.save(any())).thenReturn(saved);
        when(patientRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(doctorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Appointment res = service.bookAppointment("p2", "P2", "doc2", dt, "Checkup", null);
        assertNotNull(res);
        assertEquals("a1", res.getId());
        verify(appointmentRepository).save(any());
    }

    @Test
    void cancelAppointment_past_throws() {
        Appointment apt = new Appointment(); apt.setId("a2"); apt.setStatus(Appointment.AppointmentStatus.SCHEDULED); apt.setAppointmentDateTime(LocalDateTime.now().minusDays(1));
        when(appointmentRepository.findById("a2")).thenReturn(Optional.of(apt));
        assertThrows(BusinessLogicException.class, () -> service.cancelAppointment("a2"));
    }

    @Test
    void cancelAppointment_success() {
        Appointment apt = new Appointment(); apt.setId("a3"); apt.setStatus(Appointment.AppointmentStatus.SCHEDULED); apt.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
        when(appointmentRepository.findById("a3")).thenReturn(Optional.of(apt));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.cancelAppointment("a3");
        assertEquals(Appointment.AppointmentStatus.CANCELLED, apt.getStatus());
        verify(appointmentRepository).save(any());
    }

    @Test
    void rescheduleAppointment_notAvailable_throws() {
        Appointment apt = new Appointment(); apt.setId("a4"); apt.setDoctorId("doc3"); apt.setAppointmentDateTime(LocalDateTime.now().plusDays(3));
        when(appointmentRepository.findById("a4")).thenReturn(Optional.of(apt));

        LocalDateTime newDt = LocalDate.now().plusDays(5).atTime(9,0);
        // stub getAvailableTimeSlots via spy
        AppointmentService spy = spy(service);
        doReturn(List.of()).when(spy).getAvailableTimeSlots(eq("doc3"), eq(newDt.toLocalDate()));
        // inject repositories into spy
        // Using same mocks via constructor not necessary because spy wraps the same instance

        assertThrows(BusinessLogicException.class, () -> spy.rescheduleAppointment("a4", newDt));
    }

    @Test
    void bookAppointment_nullInputs_throws() {
        // when nulls are passed the service attempts to find a doctor and throws ResourceNotFoundException
        assertThrows(com.example.health_care_system.exception.ResourceNotFoundException.class, () -> service.bookAppointment(null, null, null, null, null, null));
    }

    @Test
    void getAvailableTimeSlots_tooFarInFuture_returnsSlots() {
        // The service does not enforce maxDaysAhead on this method; it should still return generated slots
        LocalDate farDate = LocalDate.now().plusDays(10);
        var slots = service.getAvailableTimeSlots("d1", farDate);
        assertNotNull(slots);
        // With working hours 9-17, 30 minute slots and 1 hour lunch we expect 14 slots (8 before lunch, 6 after)
        assertEquals(14, slots.size());
    }

    @Test
    void bookAppointment_missingPatient_throws() {
        Doctor doctor = new Doctor(); doctor.setId("docX"); doctor.setName("Dr X");
        when(doctorRepository.findById("docX")).thenReturn(Optional.of(doctor));
        when(patientRepository.findById("pMissing")).thenReturn(Optional.empty());

        LocalDateTime dt = LocalDate.now().plusDays(2).atTime(10,0);
        assertThrows(ResourceNotFoundException.class, () -> service.bookAppointment("pMissing", "Name", "docX", dt, null, null));
    }

    @Test
    void rescheduleAppointment_missingAppointment_throws() {
        when(appointmentRepository.findById("noApp")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.rescheduleAppointment("noApp", LocalDateTime.now().plusDays(3)));
    }

    @Test
    void rescheduleAppointment_success() {
        Appointment apt = new Appointment(); apt.setId("a5"); apt.setDoctorId("doc4"); apt.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
        when(appointmentRepository.findById("a5")).thenReturn(Optional.of(apt));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LocalDate newDate = LocalDate.now().plusDays(6);
        LocalDateTime newDt = newDate.atTime(11,0);
        AppointmentService spy = spy(service);
        doReturn(List.of(LocalTime.of(11,0))).when(spy).getAvailableTimeSlots(eq("doc4"), eq(newDate));

        // use spy for reschedule
        Appointment res = spy.rescheduleAppointment("a5", newDt);
        assertEquals(newDt, res.getAppointmentDateTime());
    }

    @Test
    void getReservedTimeSlots_excludesCurrentPatient() {
        LocalDate date = LocalDate.now().plusDays(2);
        TimeSlotReservation r1 = new TimeSlotReservation(); r1.setSlotDateTime(date.atTime(9,0)); r1.setPatientId("p1"); r1.setStatus(TimeSlotReservation.ReservationStatus.ACTIVE);
        TimeSlotReservation r2 = new TimeSlotReservation(); r2.setSlotDateTime(date.atTime(10,0)); r2.setPatientId("me"); r2.setStatus(TimeSlotReservation.ReservationStatus.ACTIVE);
        when(reservationRepository.findByDoctorIdAndStatus(eq("d1"), eq(TimeSlotReservation.ReservationStatus.ACTIVE))).thenReturn(List.of(r1, r2));

        var reserved = service.getReservedTimeSlots("d1", date, "me");
        assertTrue(reserved.contains(LocalTime.of(9,0)));
        assertFalse(reserved.contains(LocalTime.of(10,0))); // excluded because it's my reservation
    }

    @Test
    void getAvailableTimeSlots_lunchBoundary_slot14IncludedAnd13Excluded() {
        LocalDate date = LocalDate.now().plusDays(2);
        // no existing appointments or reservations
        when(appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(eq("d1"), any(), any())).thenReturn(List.of());
        when(reservationRepository.findByDoctorIdAndStatus(eq("d1"), any())).thenReturn(List.of());

        var slots = service.getAvailableTimeSlots("d1", date);
        assertFalse(slots.contains(LocalTime.of(13,0))); // lunch start should be excluded
        assertTrue(slots.contains(LocalTime.of(14,0))); // lunch end is included
    }

    @Test
    void bookAppointment_slotUnavailable_throwsBusinessLogic() {
        Doctor doctor = new Doctor(); doctor.setId("docY"); doctor.setName("DrY");
        Patient patient = new Patient(); patient.setId("pY"); patient.setName("PY");
        when(doctorRepository.findById("docY")).thenReturn(Optional.of(doctor));
        when(patientRepository.findById("pY")).thenReturn(Optional.of(patient));

        LocalDateTime dt = LocalDate.now().plusDays(2).atTime(10,0);
        AppointmentService spy = spy(service);
        doReturn(List.of()).when(spy).getAvailableTimeSlots(eq("docY"), eq(dt.toLocalDate()));

        assertThrows(BusinessLogicException.class, () -> spy.bookAppointment("pY", "PY", "docY", dt, null, null));
    }

    @Test
    void getPatientAppointments_sortsUpcomingFirst() {
        Appointment past = new Appointment(); past.setId("p1"); past.setAppointmentDateTime(LocalDateTime.now().minusDays(2));
        Appointment future = new Appointment(); future.setId("p2"); future.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
    when(appointmentRepository.findByPatientId("pat1")).thenReturn(new java.util.ArrayList<>(List.of(past, future)));

        var list = service.getPatientAppointments("pat1");
        assertEquals(2, list.size());
        assertEquals(future.getAppointmentDateTime(), list.get(0).getAppointmentDateTime());
        assertEquals(past.getAppointmentDateTime(), list.get(1).getAppointmentDateTime());
    }

    @Test
    void completeAppointment_updatesStatusAndAppendsNotes() {
        Appointment apt = new Appointment(); apt.setId("c1"); apt.setStatus(Appointment.AppointmentStatus.SCHEDULED); apt.setNotes("initial");
        when(appointmentRepository.findById("c1")).thenReturn(Optional.of(apt));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.completeAppointment("c1", "done notes");
        assertEquals(Appointment.AppointmentStatus.COMPLETED, apt.getStatus());
        assertTrue(apt.getNotes().contains("initial"));
        assertTrue(apt.getNotes().contains("done notes"));
        verify(appointmentRepository).save(any());
    }

    @Test
    void markNoShow_updatesStatus() {
        Appointment apt = new Appointment(); apt.setId("n1"); apt.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById("n1")).thenReturn(Optional.of(apt));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.markNoShow("n1");
        assertEquals(Appointment.AppointmentStatus.NO_SHOW, apt.getStatus());
        verify(appointmentRepository).save(any());
    }

    @Test
    void getAppointmentById_returnsOptional() {
        Appointment apt = new Appointment(); apt.setId("a100");
        when(appointmentRepository.findById("a100")).thenReturn(Optional.of(apt));

        var res = service.getAppointmentById("a100");
        assertTrue(res.isPresent());
        assertEquals("a100", res.get().getId());
    }
}

