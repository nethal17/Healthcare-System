package com.example.health_care_system.service;

import com.example.health_care_system.model.TimeSlotReservation;
import com.example.health_care_system.repository.TimeSlotReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimeSlotReservationServiceTest {

    @Mock
    private TimeSlotReservationRepository reservationRepository;

    private TimeSlotReservationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TimeSlotReservationService();
        TestUtils.injectField(service, "reservationRepository", reservationRepository);
    }

    @Test
    void reserveTimeSlot_alreadyReservedByOther_returnsNull() {
        LocalDateTime slot = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        TimeSlotReservation existing = new TimeSlotReservation();
        existing.setPatientId("other");
        existing.setSlotDateTime(slot);
        existing.setStatus(TimeSlotReservation.ReservationStatus.ACTIVE);
        when(reservationRepository.findByDoctorIdAndSlotDateTimeAndStatus("d1", slot, TimeSlotReservation.ReservationStatus.ACTIVE))
            .thenReturn(List.of(existing));

        var res = service.reserveTimeSlot("d1", slot, "me", "s1");
        assertNull(res);
    }

    @Test
    void reserveTimeSlot_createsReservation_and_cancelPrevious() {
        LocalDateTime slot = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0);
        when(reservationRepository.findByDoctorIdAndSlotDateTimeAndStatus("d1", slot, TimeSlotReservation.ReservationStatus.ACTIVE))
            .thenReturn(List.of());
        when(reservationRepository.findByPatientIdAndStatus("me", TimeSlotReservation.ReservationStatus.ACTIVE))
            .thenReturn(List.of());

        TimeSlotReservation saved = new TimeSlotReservation();
        saved.setId("r1");
        when(reservationRepository.save(any())).thenReturn(saved);

        var res = service.reserveTimeSlot("d1", slot, "me", "s1");
        assertNotNull(res);
        assertEquals("r1", res.getId());
    }

    @Test
    void confirm_and_cancel_and_isReserved_and_validity_and_remainingSeconds() {
        LocalDateTime slot = LocalDateTime.now().plusMinutes(1);
        TimeSlotReservation active = new TimeSlotReservation();
        active.setId("r2");
        active.setPatientId("p1");
        active.setSessionId("s1");
        active.setCreatedAt(LocalDateTime.now());
        active.setStatus(TimeSlotReservation.ReservationStatus.ACTIVE);

        when(reservationRepository.findByPatientIdAndSessionIdAndStatus("p1","s1",TimeSlotReservation.ReservationStatus.ACTIVE))
            .thenReturn(Optional.of(active));

        // confirm
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        service.confirmReservation("p1","s1");
        assertEquals(TimeSlotReservation.ReservationStatus.CONFIRMED, active.getStatus());

        // cancel
        active.setStatus(TimeSlotReservation.ReservationStatus.ACTIVE);
        when(reservationRepository.findByPatientIdAndSessionIdAndStatus("p1","s1",TimeSlotReservation.ReservationStatus.ACTIVE))
            .thenReturn(Optional.of(active));
        service.cancelReservation("p1","s1");
        assertEquals(TimeSlotReservation.ReservationStatus.CANCELLED, active.getStatus());

        // isReserved
        TimeSlotReservation other = new TimeSlotReservation(); other.setPatientId("other"); other.setSlotDateTime(slot);
        when(reservationRepository.findByDoctorIdAndSlotDateTimeAndStatus("d1", slot, TimeSlotReservation.ReservationStatus.ACTIVE))
            .thenReturn(List.of(other));
        assertTrue(service.isSlotReserved("d1", slot, "me"));

        // validity and remaining seconds: simulate present active reservation
        active.setStatus(TimeSlotReservation.ReservationStatus.ACTIVE);
        active.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        when(reservationRepository.findByPatientIdAndSessionIdAndStatus("p1","s1",TimeSlotReservation.ReservationStatus.ACTIVE))
            .thenReturn(Optional.of(active));
        assertTrue(service.isReservationValid("p1","s1"));
        long remaining = service.getRemainingSeconds("p1","s1");
        assertTrue(remaining >= 0);
    }
}

