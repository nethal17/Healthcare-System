package com.example.health_care_system.service;

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
import static org.mockito.Mockito.*;

class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private TimeSlotReservationRepository reservationRepository;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AppointmentService();
        // Inject mocks via reflection since fields are private
        TestUtils.injectField(service, "appointmentRepository", appointmentRepository);
        TestUtils.injectField(service, "doctorRepository", doctorRepository);
        TestUtils.injectField(service, "patientRepository", patientRepository);
        TestUtils.injectField(service, "reservationRepository", reservationRepository);
    }

    @Test
    void getAvailableTimeSlots_pastDate_throws() {
        LocalDate past = LocalDate.now().minusDays(1);
        assertThrows(RuntimeException.class, () -> service.getAvailableTimeSlots("d1", past));
    }

    @Test
    void getAvailableTimeSlots_filtersBookedAndReserved() {
        LocalDate date = LocalDate.now().plusDays(2);
        // One scheduled appointment at 9:00
        Appointment apt = new Appointment();
        apt.setAppointmentDateTime(date.atTime(9,0));
        apt.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(eq("d1"), any(), any()))
            .thenReturn(List.of(apt));

        TimeSlotReservation res = new TimeSlotReservation();
        res.setSlotDateTime(date.atTime(9,30));
        res.setPatientId("pX");
        res.setStatus(TimeSlotReservation.ReservationStatus.ACTIVE);
        when(reservationRepository.findByDoctorIdAndStatus("d1", TimeSlotReservation.ReservationStatus.ACTIVE))
            .thenReturn(List.of(res));

        List<LocalTime> slots = service.getAvailableTimeSlots("d1", date, null);
        // Ensure 9:00 and 9:30 are not available
        assertFalse(slots.contains(LocalTime.of(9,0)));
        assertFalse(slots.contains(LocalTime.of(9,30)));
        assertTrue(slots.contains(LocalTime.of(10,0)));
    }

    @Test
    void bookAppointment_success() {
        String docId = "doc1";
        String patId = "pat1";
        Doctor doctor = new Doctor(); doctor.setId(docId); doctor.setName("Dr A");
        Patient patient = new Patient(); patient.setId(patId); patient.setName("P A");
        when(doctorRepository.findById(docId)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(patId)).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetween(any(), any(), any()))
            .thenReturn(List.of());
        when(reservationRepository.findByDoctorIdAndStatus(eq(docId), any())).thenReturn(List.of());

        Appointment saved = new Appointment(); saved.setId("a1");
        when(appointmentRepository.save(any())).thenReturn(saved);
        when(patientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(doctorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime dt = LocalDate.now().plusDays(2).atTime(10,0);
        Appointment result = service.bookAppointment(patId, "P A", docId, dt, "Checkup", null);
        assertNotNull(result);
        assertEquals("a1", result.getId());
        verify(appointmentRepository).save(any());
        verify(patientRepository).save(any());
        verify(doctorRepository).save(any());
    }

    @Test
    void bookAppointment_doctorNotFound_throws() {
        when(doctorRepository.findById("dX")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.bookAppointment("p","pname","dX", LocalDateTime.now().plusDays(2), null, null));
    }

    @Test
    void cancel_complete_markNoShow_and_getById() {
        Appointment apt = new Appointment();
        apt.setId("a2");
        apt.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        apt.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        when(appointmentRepository.findById("a2")).thenReturn(Optional.of(apt));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.cancelAppointment("a2");
        assertEquals(Appointment.AppointmentStatus.CANCELLED, apt.getStatus());

        // complete
        apt.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById("a2")).thenReturn(Optional.of(apt));
        service.completeAppointment("a2", "notes");
        assertEquals(Appointment.AppointmentStatus.COMPLETED, apt.getStatus());
        assertTrue(apt.getNotes().contains("notes"));

        // no-show
        apt.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById("a2")).thenReturn(Optional.of(apt));
        service.markNoShow("a2");
        assertEquals(Appointment.AppointmentStatus.NO_SHOW, apt.getStatus());

        when(appointmentRepository.findById("a2")).thenReturn(Optional.of(apt));
        Optional<Appointment> opt = service.getAppointmentById("a2");
        assertTrue(opt.isPresent());
    }

    @Test
    void rescheduleAppointment_success_and_failure() {
        Appointment apt = new Appointment();
        apt.setId("a3");
        apt.setDoctorId("doc1");
        apt.setAppointmentDateTime(LocalDateTime.now().plusDays(3));
        when(appointmentRepository.findById("a3")).thenReturn(Optional.of(apt));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // mock available slots to include new time
        LocalDate newDate = LocalDate.now().plusDays(4);
        // Make getAvailableTimeSlots return list containing time
        AppointmentService spy = spy(service);
        doReturn(List.of(LocalTime.of(11,0))).when(spy).getAvailableTimeSlots(eq("doc1"), eq(newDate));

        LocalDateTime newDateTime = newDate.atTime(11,0);
        TestUtils.injectField(spy, "appointmentRepository", appointmentRepository);
        Appointment res = spy.rescheduleAppointment("a3", newDateTime);
        assertEquals(newDateTime, res.getAppointmentDateTime());

        // failure: not available
        doReturn(List.of()).when(spy).getAvailableTimeSlots(eq("doc1"), eq(newDate.plusDays(1)));
        assertThrows(RuntimeException.class, () -> spy.rescheduleAppointment("a3", newDate.plusDays(1).atTime(9,0)));
    }
}

