package com.example.health_care_system.service;

import com.example.health_care_system.builder.EmailTemplateBuilder;
import com.example.health_care_system.exception.EmailSendException;
import com.example.health_care_system.model.*;
import jakarta.mail.MessagingException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceAdditionalTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private EmailTemplateBuilder templateBuilder;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(mailSender, templateEngine, templateBuilder);
        // set fromEmail via reflection
        org.springframework.test.util.ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@example.com");
    }

    @Test
    void sendGovernmentAppointmentConfirmation_messagingException_throws() throws Exception {
        Patient p = new Patient(); p.setEmail("p@ex.com");
        Appointment a = new Appointment(); a.setId("apt1"); a.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        Doctor d = new Doctor(); d.setName("Dr");
        Hospital h = new Hospital(); h.setName("H1");

    when(templateBuilder.buildAppointmentContext(eq(p), eq(a), eq(d), eq(h))).thenReturn(new Context());
    when(templateEngine.process(eq("emails/appointment-confirmation-government"), any(Context.class))).thenReturn("<html></html>");
    // mailSender.createMimeMessage cannot be stubbed to throw a checked exception directly; return a mock and cause helper to fail
    jakarta.mail.internet.MimeMessage mime = mock(jakarta.mail.internet.MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(mime);

    // Simulate failure during send by making mailSender.send throw a runtime exception
    doThrow(new RuntimeException("send failed")).when(mailSender).send(any(MimeMessage.class));

    // The service does not catch RuntimeException from mailSender.send, so expect a runtime exception
    RuntimeException ex = assertThrows(RuntimeException.class, () -> emailService.sendGovernmentAppointmentConfirmation(p, a, d, h));
    assertTrue(ex.getMessage().toLowerCase().contains("send failed"));
    }

    @Test
    void sendCardPaymentAppointmentConfirmation_success() throws Exception {
        Patient p = new Patient(); p.setEmail("p@ex.com");
        Appointment a = new Appointment(); a.setId("apt2"); a.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        Doctor d = new Doctor(); d.setName("Dr");
        Hospital h = new Hospital(); h.setName("H2");
        Payment payment = new Payment(); payment.setId("pay1"); payment.setAmount(new BigDecimal("200")); payment.setPaymentMethod(Payment.PaymentMethod.CARD);

        when(templateBuilder.buildAppointmentContext(eq(p), eq(a), eq(d), eq(h))).thenReturn(new Context());
        when(templateBuilder.addPaymentInfo(any(Context.class), eq(payment))).thenReturn(new Context());
        when(templateEngine.process(eq("emails/appointment-confirmation-card"), any(Context.class))).thenReturn("<html></html>");
        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);

        assertDoesNotThrow(() -> emailService.sendCardPaymentAppointmentConfirmation(p, a, d, h, payment));
        verify(mailSender).send(mime);
    }

    @Test
    void sendCardPaymentAppointmentConfirmation_nullTemplate_throws() throws Exception {
        Patient p = new Patient(); p.setEmail("p@ex.com");
        Appointment a = new Appointment(); a.setId("apt3"); a.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        Doctor d = new Doctor(); d.setName("Dr");
        Hospital h = new Hospital(); h.setName("H2");
        Payment payment = new Payment(); payment.setId("pay2");

        when(templateBuilder.buildAppointmentContext(eq(p), eq(a), eq(d), eq(h))).thenReturn(new Context());
        when(templateBuilder.addPaymentInfo(any(Context.class), eq(payment))).thenReturn(new Context());
        when(templateEngine.process(eq("emails/appointment-confirmation-card"), any(Context.class))).thenReturn(null);
        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);

        // The MimeMessageHelper will throw IllegalArgumentException when setText is called with null; expect that
        assertThrows(IllegalArgumentException.class, () -> emailService.sendCardPaymentAppointmentConfirmation(p, a, d, h, payment));
    }

    @Test
    void sendGovernmentAppointmentConfirmation_nullMail_throws() throws Exception {
        Patient p = new Patient(); p.setEmail("p@ex.com");
        Appointment a = new Appointment(); a.setId("apt4"); a.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        Doctor d = new Doctor(); d.setName("Dr");
        Hospital h = new Hospital(); h.setName("H1");

        when(templateBuilder.buildAppointmentContext(eq(p), eq(a), eq(d), eq(h))).thenReturn(new Context());
        when(templateEngine.process(eq("emails/appointment-confirmation-government"), any(Context.class))).thenReturn("<html></html>");
        when(mailSender.createMimeMessage()).thenReturn(null);

        // Creating a MimeMessageHelper with a null message will throw a RuntimeException (or NPE)
        assertThrows(RuntimeException.class, () -> emailService.sendGovernmentAppointmentConfirmation(p, a, d, h));
    }
}

