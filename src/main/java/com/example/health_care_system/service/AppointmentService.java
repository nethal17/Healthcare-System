package com.example.health_care_system.service;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.TimeSlotReservation;
import com.example.health_care_system.repository.AppointmentRepository;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.PatientRepository;
import com.example.health_care_system.repository.TimeSlotReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private TimeSlotReservationRepository reservationRepository;
    
    // Working hours configuration
    private static final LocalTime WORKING_START = LocalTime.of(9, 0);  // 9:00 AM
    private static final LocalTime WORKING_END = LocalTime.of(17, 0);   // 5:00 PM
    private static final LocalTime LUNCH_START = LocalTime.of(13, 0);   // 1:00 PM
    private static final LocalTime LUNCH_END = LocalTime.of(14, 0);     // 2:00 PM
    private static final int SLOT_DURATION_MINUTES = 30;                 // 30-minute slots
    
    /**
     * Get available time slots for a doctor on a specific date
     * Excludes booked appointments and currently reserved slots
     */
    public List<LocalTime> getAvailableTimeSlots(String doctorId, LocalDate date) {
        // Validate date is not in the past
        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot book appointments for past dates");
        }
        return getAvailableTimeSlots(doctorId, date, null);
    }
    
    /**
     * Get available time slots for a doctor on a specific date
     * Excludes booked appointments and reserved slots (except for the specified patient)
     * @param excludePatientId - Patient ID to exclude from reservation check (their own reservation)
     */
    public List<LocalTime> getAvailableTimeSlots(String doctorId, LocalDate date, String excludePatientId) {
        // Validate date is not in the past
        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot book appointments for past dates");
        }
        
        List<LocalTime> allSlots = generateAllTimeSlots();
        
        // Get existing appointments for this doctor on this date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        List<Appointment> existingAppointments = appointmentRepository
            .findByDoctorIdAndAppointmentDateTimeBetween(doctorId, startOfDay, endOfDay)
            .stream()
            .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
            .toList();
        
        // Remove booked slots
        Set<LocalTime> bookedSlots = existingAppointments.stream()
            .map(apt -> apt.getAppointmentDateTime().toLocalTime())
            .collect(Collectors.toSet());
        
        // Get active reservations for this doctor (excluding the current patient)
        List<TimeSlotReservation> activeReservations = reservationRepository
            .findByDoctorIdAndStatus(
                doctorId, 
                TimeSlotReservation.ReservationStatus.ACTIVE
            );
        
        // Filter reservations for the specific date and exclude current patient's reservations
        Set<LocalTime> reservedSlots = activeReservations.stream()
            .filter(res -> res.getSlotDateTime().toLocalDate().equals(date))
            .filter(res -> excludePatientId == null || !res.getPatientId().equals(excludePatientId))
            .map(res -> res.getSlotDateTime().toLocalTime())
            .collect(Collectors.toSet());
        
        // Filter out booked and reserved slots
        List<LocalTime> availableSlots = allSlots.stream()
            .filter(slot -> !bookedSlots.contains(slot))
            .filter(slot -> !reservedSlots.contains(slot))
            .collect(Collectors.toList());
        
        // If the date is today, filter out past time slots
        if (date.equals(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            availableSlots = availableSlots.stream()
                .filter(slot -> slot.isAfter(now.plusHours(1))) // Need at least 1 hour notice
                .collect(Collectors.toList());
        }
        
        return availableSlots;
    }
    
    /**
     * Get reserved time slots for a doctor on a specific date (by other users)
     * @param excludePatientId - Patient ID to exclude from reserved slots (their own reservation)
     */
    public List<LocalTime> getReservedTimeSlots(String doctorId, LocalDate date, String excludePatientId) {
        // Get active reservations for this doctor
        List<TimeSlotReservation> activeReservations = reservationRepository
            .findByDoctorIdAndStatus(
                doctorId, 
                TimeSlotReservation.ReservationStatus.ACTIVE
            );
        
        // Filter reservations for the specific date and exclude current patient's reservations
        List<LocalTime> reservedSlots = activeReservations.stream()
            .filter(res -> res.getSlotDateTime().toLocalDate().equals(date))
            .filter(res -> excludePatientId == null || !res.getPatientId().equals(excludePatientId))
            .map(res -> res.getSlotDateTime().toLocalTime())
            .sorted()
            .collect(Collectors.toList());
        
        return reservedSlots;
    }
    
    /**
     * Generate all possible time slots in a day
     */
    private List<LocalTime> generateAllTimeSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime currentSlot = WORKING_START;
        
        while (currentSlot.isBefore(WORKING_END)) {
            // Skip lunch hour
            if (currentSlot.isBefore(LUNCH_START) || currentSlot.isAfter(LUNCH_END) || currentSlot.equals(LUNCH_END)) {
                slots.add(currentSlot);
            }
            currentSlot = currentSlot.plusMinutes(SLOT_DURATION_MINUTES);
        }
        
        return slots;
    }
    
    /**
     * Get next 7 days for date selection
     */
    public List<LocalDate> getNextSevenDays() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < 7; i++) {
            dates.add(today.plusDays(i));
        }
        
        return dates;
    }
    
    /**
     * Book an appointment
     */
    @Transactional
    public Appointment bookAppointment(
            String patientId,
            String patientName,
            String doctorId,
            LocalDateTime appointmentDateTime,
            String purpose,
            String notes) {
        
        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        // Validate patient exists
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        // CRITICAL: Check if appointment already exists for this exact time slot (prevent double booking)
        List<Appointment> existingAppointments = appointmentRepository
            .findByDoctorIdAndAppointmentDateTimeBetween(
                doctorId, 
                appointmentDateTime.minusSeconds(1), 
                appointmentDateTime.plusSeconds(1)
            );
        
        // Check if any scheduled appointment exists for this slot
        boolean slotAlreadyBooked = existingAppointments.stream()
            .anyMatch(apt -> apt.getStatus() == Appointment.AppointmentStatus.SCHEDULED);
        
        if (slotAlreadyBooked) {
            throw new RuntimeException("This time slot has just been booked by another patient. Please select a different time.");
        }
        
        // Additional validation: Check if the time slot is in the available slots
        LocalDate date = appointmentDateTime.toLocalDate();
        List<LocalTime> availableSlots = getAvailableTimeSlots(doctorId, date);
        
        if (!availableSlots.contains(appointmentDateTime.toLocalTime())) {
            throw new RuntimeException("Selected time slot is no longer available");
        }
        
        // Create appointment (using MongoDB ObjectIds)
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);  // MongoDB ObjectId
        appointment.setPatientName(patientName);
        appointment.setDoctorId(doctorId);  // MongoDB ObjectId
        appointment.setDoctorName(doctor.getName());
        appointment.setAppointmentDateTime(appointmentDateTime);
        appointment.setPurpose(purpose != null ? purpose : "General Consultation");
        appointment.setNotes(notes);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        
        // Save appointment (MongoDB will auto-generate the id)
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Update patient's appointments list
        if (patient.getAppointments() == null) {
            patient.setAppointments(new ArrayList<>());
        }
        patient.getAppointments().add(savedAppointment);
        patientRepository.save(patient);
        
        // Update doctor's appointments list
        if (doctor.getAppointments() == null) {
            doctor.setAppointments(new ArrayList<>());
        }
        doctor.getAppointments().add(savedAppointment);
        doctorRepository.save(doctor);
        
        return savedAppointment;
    }
    
    /**
     * Get patient's appointments
     */
    public List<Appointment> getPatientAppointments(String patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        
        // Sort by date (upcoming first, then past)
        appointments.sort((a1, a2) -> {
            if (a1.getAppointmentDateTime().isBefore(LocalDateTime.now()) && 
                a2.getAppointmentDateTime().isAfter(LocalDateTime.now())) {
                return 1;
            } else if (a1.getAppointmentDateTime().isAfter(LocalDateTime.now()) && 
                       a2.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
                return -1;
            } else {
                return a1.getAppointmentDateTime().compareTo(a2.getAppointmentDateTime());
            }
        });
        
        return appointments;
    }
    
    /**
     * Get doctor's appointments
     */
    public List<Appointment> getDoctorAppointments(String doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }
    
    /**
     * Cancel an appointment
     */
    public void cancelAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Only allow cancellation of scheduled appointments
        if (appointment.getStatus() != Appointment.AppointmentStatus.SCHEDULED) {
            throw new RuntimeException("Only scheduled appointments can be cancelled");
        }
        
        // Check if appointment is in the future
        if (appointment.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot cancel past appointments");
        }
        
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }
    
    /**
     * Complete an appointment
     */
    public void completeAppointment(String appointmentId, String notes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        if (notes != null && !notes.isEmpty()) {
            appointment.setNotes(appointment.getNotes() + "\n" + notes);
        }
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }
    
    /**
     * Mark appointment as no-show
     */
    public void markNoShow(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        appointment.setStatus(Appointment.AppointmentStatus.NO_SHOW);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }
    
    /**
     * Get appointment by MongoDB ObjectId
     */
    public Optional<Appointment> getAppointmentById(String appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }
    
    /**
     * Reschedule an appointment
     */
    public Appointment rescheduleAppointment(String appointmentId, LocalDateTime newDateTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Check if the new time slot is available
        LocalDate newDate = newDateTime.toLocalDate();
        List<LocalTime> availableSlots = getAvailableTimeSlots(appointment.getDoctorId(), newDate);
        
        if (!availableSlots.contains(newDateTime.toLocalTime())) {
            throw new RuntimeException("Selected time slot is not available");
        }
        
        appointment.setAppointmentDateTime(newDateTime);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        return appointmentRepository.save(appointment);
    }
}
