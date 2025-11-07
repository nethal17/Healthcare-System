package com.example.health_care_system.controller;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentRepository appointmentRepository;

    @Test
    void getPatientTrends_returnsMonthlyCounts() throws Exception {
        // prepare test data
        Appointment ap1 = new Appointment();
        ap1.setPatientId("patient1");
        ap1.setAppointmentDateTime(LocalDateTime.of(2024, 1, 10, 10, 0));
        Appointment ap2 = new Appointment();
        ap2.setPatientId("patient1");
        ap2.setAppointmentDateTime(LocalDateTime.of(2024, 1, 20, 11, 0));
        Appointment ap3 = new Appointment();
        ap3.setPatientId("patient1");
        ap3.setAppointmentDateTime(LocalDateTime.of(2024, 2, 5, 9, 0));
        Mockito.when(appointmentRepository.findByPatientId("patient1"))
                .thenReturn(List.of(ap1, ap2, ap3));

        mockMvc.perform(get("/api/analytics/patient-trends")
                .param("patientId", "patient1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.2024-01", is(2)))
            .andExpect(jsonPath("$.2024-02", is(1)));
    }
}

