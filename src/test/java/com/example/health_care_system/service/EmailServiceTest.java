package com.example.health_care_system.service;

import com.example.health_care_system.model.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private TemplateEngine templateEngine;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService();
        TestUtils.injectField(emailService, "mailSender", mailSender);
        TestUtils.injectField(emailService, "templateEngine", templateEngine);
        TestUtils.injectField(emailService, "fromEmail", "no-reply@example.com");
        TestUtils.injectField(emailService, "baseUrl", "http://localhost:8080");
    }

    @Test
    void sendGovernmentAppointmentConfirmation_sends() throws Exception {
        Patient p = new Patient(); p.setName("P"); p.setEmail("p@example.com");
        Appointment a = new Appointment(); a.setId("apt1"); a.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        Doctor d = new Doctor(); d.setName("Dr"); d.setSpecialization("Gen");
        Hospital h = new Hospital();
        Hospital.Location loc = new Hospital.Location(); loc.setAddress("Addr"); loc.setCity("City");
        h.setLocation(loc);
        Hospital.ContactInfo ci = new Hospital.ContactInfo(); ci.setEmail("h@ex.com"); ci.setPhoneNumber("123");
        h.setContactInfo(ci);
        when(templateEngine.process(eq("emails/appointment-confirmation-government"), any(Context.class))).thenReturn("<html></html>");
        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);

        emailService.sendGovernmentAppointmentConfirmation(p,a,d,h);
        verify(mailSender).send(mime);
    }

    @Test
    void sendCashPaymentAppointmentConfirmation_sends() throws Exception {
        Patient p = new Patient(); p.setName("P"); p.setEmail("p@example.com");
        Appointment a = new Appointment(); a.setId("apt2"); a.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        Doctor d = new Doctor(); d.setName("Dr"); d.setSpecialization("Gen");
        Hospital h = new Hospital();
        Hospital.Location loc = new Hospital.Location(); loc.setAddress("Addr"); loc.setCity("City");
        h.setLocation(loc);
        Hospital.ContactInfo ci = new Hospital.ContactInfo(); ci.setEmail("h@ex.com"); ci.setPhoneNumber("123");
        h.setContactInfo(ci);
        Payment payment = new Payment(); payment.setAmount(new BigDecimal("1500")); payment.setId("pay1");
        when(templateEngine.process(eq("emails/appointment-confirmation-cash"), any(Context.class))).thenReturn("<html></html>");
        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);

        emailService.sendCashPaymentAppointmentConfirmation(p,a,d,h,payment);
        verify(mailSender).send(mime);
    }
}

