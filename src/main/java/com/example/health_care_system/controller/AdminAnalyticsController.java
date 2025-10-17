package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.*;
import com.example.health_care_system.repository.*;
import com.example.health_care_system.service.AnalyticsPdfService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/analytics")
public class AdminAnalyticsController {

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private AnalyticsPdfService analyticsPdfService;

    /**
     * Main analytics dashboard
     */
    @GetMapping("")
    public String viewAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session,
            Model model) {
        
        // Check if user is admin
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        // Set default date range (last 30 days)
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        // Get all appointments in date range
        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDateTime() != null)
                .filter(a -> !a.getAppointmentDateTime().isBefore(startDateTime) && 
                           !a.getAppointmentDateTime().isAfter(endDateTime))
                .collect(Collectors.toList());
        
        // 1. Most booked time slots
        Map<String, Long> timeSlotData = getTimeSlotAnalysis(appointments);
        
        // 2. Appointments by day of week
        Map<String, Long> dayOfWeekData = getDayOfWeekAnalysis(appointments);
        
        // 3. Top doctors by appointment count
        Map<String, Long> topDoctorsData = getTopDoctorsAnalysis(appointments);
        
        // 4. Specialization demand
        Map<String, Long> specializationData = getSpecializationAnalysis(appointments);
        
        // 5. Daily appointment trend
        Map<String, Long> dailyTrendData = getDailyTrendAnalysis(appointments);
        
        // 6. Appointment status distribution
        Map<String, Long> statusData = getStatusAnalysis(appointments);
        
        // 7. Monthly comparison
        Map<String, Long> monthlyData = getMonthlyAnalysis();
        
        // 8. Peak hours analysis
        Map<String, Long> peakHoursData = getPeakHoursAnalysis(appointments);
        
        // Summary statistics
        int totalAppointments = appointments.size();
        int scheduledCount = (int) appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                .count();
        int completedCount = (int) appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .count();
        int cancelledCount = (int) appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                .count();
        
        // Calculate rates
        double completionRate = totalAppointments > 0 ? (completedCount * 100.0 / totalAppointments) : 0;
        double cancellationRate = totalAppointments > 0 ? (cancelledCount * 100.0 / totalAppointments) : 0;
        
        // Get unique counts
        long uniqueDoctors = appointments.stream()
                .map(Appointment::getDoctorId)
                .distinct()
                .count();
        long uniquePatients = appointments.stream()
                .map(Appointment::getPatientId)
                .distinct()
                .count();
        
        // Add to model
        model.addAttribute("user", user);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        model.addAttribute("totalAppointments", totalAppointments);
        model.addAttribute("scheduledCount", scheduledCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("completionRate", String.format("%.1f", completionRate));
        model.addAttribute("cancellationRate", String.format("%.1f", cancellationRate));
        model.addAttribute("uniqueDoctors", uniqueDoctors);
        model.addAttribute("uniquePatients", uniquePatients);
        
        model.addAttribute("timeSlotData", timeSlotData);
        model.addAttribute("dayOfWeekData", dayOfWeekData);
        model.addAttribute("topDoctorsData", topDoctorsData);
        model.addAttribute("specializationData", specializationData);
        model.addAttribute("dailyTrendData", dailyTrendData);
        model.addAttribute("statusData", statusData);
        model.addAttribute("monthlyData", monthlyData);
        model.addAttribute("peakHoursData", peakHoursData);
        
        return "admin/analytics";
    }
    
    /**
     * Analyze time slot bookings (grouped by hour ranges)
     */
    private Map<String, Long> getTimeSlotAnalysis(List<Appointment> appointments) {
        Map<String, Long> timeSlots = new LinkedHashMap<>();
        
        for (Appointment apt : appointments) {
            int hour = apt.getAppointmentDateTime().getHour();
            String slot;
            
            if (hour >= 8 && hour < 10) {
                slot = "08:00 - 10:00";
            } else if (hour >= 10 && hour < 12) {
                slot = "10:00 - 12:00";
            } else if (hour >= 12 && hour < 14) {
                slot = "12:00 - 14:00";
            } else if (hour >= 14 && hour < 16) {
                slot = "14:00 - 16:00";
            } else if (hour >= 16 && hour < 18) {
                slot = "16:00 - 18:00";
            } else {
                slot = "Other Hours";
            }
            
            timeSlots.put(slot, timeSlots.getOrDefault(slot, 0L) + 1);
        }
        
        return timeSlots.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * Analyze appointments by day of week
     */
    private Map<String, Long> getDayOfWeekAnalysis(List<Appointment> appointments) {
        Map<DayOfWeek, Long> dayCount = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAppointmentDateTime().getDayOfWeek(),
                        Collectors.counting()
                ));
        
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("Monday", dayCount.getOrDefault(DayOfWeek.MONDAY, 0L));
        result.put("Tuesday", dayCount.getOrDefault(DayOfWeek.TUESDAY, 0L));
        result.put("Wednesday", dayCount.getOrDefault(DayOfWeek.WEDNESDAY, 0L));
        result.put("Thursday", dayCount.getOrDefault(DayOfWeek.THURSDAY, 0L));
        result.put("Friday", dayCount.getOrDefault(DayOfWeek.FRIDAY, 0L));
        result.put("Saturday", dayCount.getOrDefault(DayOfWeek.SATURDAY, 0L));
        result.put("Sunday", dayCount.getOrDefault(DayOfWeek.SUNDAY, 0L));
        
        return result;
    }
    
    /**
     * Get top 10 doctors by appointment count
     */
    private Map<String, Long> getTopDoctorsAnalysis(List<Appointment> appointments) {
        return appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDoctorName() != null ? a.getDoctorName() : "Unknown",
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * Analyze specialization demand
     */
    private Map<String, Long> getSpecializationAnalysis(List<Appointment> appointments) {
        Map<String, Long> specializationCount = new HashMap<>();
        List<Doctor> allDoctors = doctorRepository.findAll();
        
        for (Appointment apt : appointments) {
            Doctor doctor = allDoctors.stream()
                    .filter(d -> d.getId().equals(apt.getDoctorId()))
                    .findFirst()
                    .orElse(null);
            
            if (doctor != null && doctor.getSpecialization() != null) {
                String spec = doctor.getSpecialization();
                specializationCount.put(spec, specializationCount.getOrDefault(spec, 0L) + 1);
            }
        }
        
        return specializationCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * Daily appointment trend
     */
    private Map<String, Long> getDailyTrendAnalysis(List<Appointment> appointments) {
        return appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAppointmentDateTime().toLocalDate().toString(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * Appointment status distribution
     */
    private Map<String, Long> getStatusAnalysis(List<Appointment> appointments) {
        return appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStatus().toString(),
                        Collectors.counting()
                ));
    }
    
    /**
     * Monthly comparison (last 6 months)
     */
    private Map<String, Long> getMonthlyAnalysis() {
        List<Appointment> allAppointments = appointmentRepository.findAll();
        Map<String, Long> monthlyCount = new LinkedHashMap<>();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            String monthKey = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + date.getYear();
            
            long count = allAppointments.stream()
                    .filter(a -> a.getAppointmentDateTime() != null)
                    .filter(a -> {
                        LocalDate aptDate = a.getAppointmentDateTime().toLocalDate();
                        return aptDate.getMonth() == date.getMonth() && 
                               aptDate.getYear() == date.getYear();
                    })
                    .count();
            
            monthlyCount.put(monthKey, count);
        }
        
        return monthlyCount;
    }
    
    /**
     * Peak hours analysis (hourly distribution)
     */
    private Map<String, Long> getPeakHoursAnalysis(List<Appointment> appointments) {
        Map<Integer, Long> hourCount = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAppointmentDateTime().getHour(),
                        Collectors.counting()
                ));
        
        Map<String, Long> result = new LinkedHashMap<>();
        for (int hour = 8; hour <= 18; hour++) {
            String hourLabel = String.format("%02d:00", hour);
            result.put(hourLabel, hourCount.getOrDefault(hour, 0L));
        }
        
        return result;
    }
    
    /**
     * Export analytics as PDF
     */
    @GetMapping("/export-pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {
        
        // Check if user is admin
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            // Set default date range (last 30 days)
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            // Get all appointments in date range
            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(a -> a.getAppointmentDateTime() != null)
                    .filter(a -> !a.getAppointmentDateTime().isBefore(startDateTime) && 
                               !a.getAppointmentDateTime().isAfter(endDateTime))
                    .collect(Collectors.toList());
            
            // Get all analysis data
            Map<String, Long> timeSlotData = getTimeSlotAnalysis(appointments);
            Map<String, Long> dayOfWeekData = getDayOfWeekAnalysis(appointments);
            Map<String, Long> topDoctorsData = getTopDoctorsAnalysis(appointments);
            Map<String, Long> specializationData = getSpecializationAnalysis(appointments);
            Map<String, Long> statusData = getStatusAnalysis(appointments);
            Map<String, Long> monthlyData = getMonthlyAnalysis();
            Map<String, Long> peakHoursData = getPeakHoursAnalysis(appointments);
            
            // Summary statistics
            int totalAppointments = appointments.size();
            int scheduledCount = (int) appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                    .count();
            int completedCount = (int) appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                    .count();
            int cancelledCount = (int) appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                    .count();
            
            double completionRate = totalAppointments > 0 ? (completedCount * 100.0 / totalAppointments) : 0;
            
            long uniqueDoctors = appointments.stream()
                    .map(Appointment::getDoctorId)
                    .distinct()
                    .count();
            long uniquePatients = appointments.stream()
                    .map(Appointment::getPatientId)
                    .distinct()
                    .count();
            
            // Generate PDF
            byte[] pdfBytes = analyticsPdfService.generateAnalyticsReport(
                    startDate,
                    endDate,
                    totalAppointments,
                    String.format("%.1f", completionRate),
                    uniqueDoctors,
                    uniquePatients,
                    scheduledCount,
                    completedCount,
                    cancelledCount,
                    timeSlotData,
                    dayOfWeekData,
                    topDoctorsData,
                    specializationData,
                    statusData,
                    monthlyData,
                    peakHoursData
            );
            
            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "analytics-report-" + startDate + "-to-" + endDate + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
