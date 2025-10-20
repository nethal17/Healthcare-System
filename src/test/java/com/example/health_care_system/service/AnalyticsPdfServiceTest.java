package com.example.health_care_system.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsPdfServiceTest {

    private final AnalyticsPdfService service = new AnalyticsPdfService();

    @Test
    void generateAnalyticsReport_returnsBytes() throws Exception {
        Map<String, Long> empty = new HashMap<>();
        Map<String, Long> timeSlot = Map.of("09:00", 5L);
        Map<String, Long> dayOfWeek = Map.of("Monday", 10L);
        Map<String, Long> topDoctors = Map.of("Dr A", 7L);
        Map<String, Long> specialization = Map.of("Cardio", 4L);
        Map<String, Long> status = Map.of("SCHEDULED", 6L);
        Map<String, Long> monthly = Map.of("2025-01", 8L);
        Map<String, Long> peak = Map.of("10", 3L);

        byte[] pdf = service.generateAnalyticsReport(
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                100,
                "85",
                10L,
                50L,
                60,
                30,
                10,
                timeSlot,
                dayOfWeek,
                topDoctors,
                specialization,
                status,
                monthly,
                peak
        );

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generateAnalyticsReport_withEmptyMaps_returnsBytes() throws Exception {
        Map<String, Long> empty = new HashMap<>();
        byte[] pdf = service.generateAnalyticsReport(
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                0,
                "0",
                0L,
                0L,
                0,
                0,
                0,
                empty,
                empty,
                empty,
                empty,
                empty,
                empty,
                empty
        );
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}

