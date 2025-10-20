package com.example.health_care_system.service;

import com.example.health_care_system.dto.PaymentRequest;
import com.example.health_care_system.dto.StripeResponse;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class StripeServiceUnitTest {

    @Test
    void checkoutProducts_success() throws Exception {
        StripeService realService = new StripeService();
    // inject secretKey and serverPort
    com.example.health_care_system.service.TestUtils.injectField(realService, "secretKey", "sk_test_123");
    com.example.health_care_system.service.TestUtils.injectField(realService, "serverPort", "8080");

        PaymentRequest req = new PaymentRequest(500L, 1L, "Consultation", "lkr");

        // Spy the service and stub createSession
        StripeService spyService = Mockito.spy(realService);

        Session mockSession = Mockito.mock(Session.class);
        Mockito.when(mockSession.getId()).thenReturn("sess_123");
        Mockito.when(mockSession.getUrl()).thenReturn("https://checkout.stripe/sess_123");

        Mockito.doReturn(mockSession).when(spyService).createSession(Mockito.any());

        StripeResponse resp = spyService.checkoutProducts(req);
        assertNotNull(resp);
        assertEquals("SUCCESS", resp.getStatus());
        assertEquals("sess_123", resp.getSessionId());
        assertEquals("https://checkout.stripe/sess_123", resp.getSessionUrl());
    }

    @Test
    void checkoutProducts_createSessionThrows_returnsFailed() throws Exception {
        StripeService realService = new StripeService();
    com.example.health_care_system.service.TestUtils.injectField(realService, "secretKey", "sk_test_123");
    com.example.health_care_system.service.TestUtils.injectField(realService, "serverPort", "8080");

        PaymentRequest req = new PaymentRequest(500L, 1L, "Consultation", "lkr");

        StripeService spyService = Mockito.spy(realService);
        Mockito.doThrow(new RuntimeException("Stripe down")).when(spyService).createSession(Mockito.any());

        var resp = spyService.checkoutProducts(req);
        assertNotNull(resp);
        assertEquals("FAILED", resp.getStatus());
        assertTrue(resp.getMessage().toLowerCase().contains("failed"));
    }

    @Test
    void checkoutProducts_zeroAmount_returnsFailed() throws Exception {
        StripeService realService = new StripeService();
    com.example.health_care_system.service.TestUtils.injectField(realService, "secretKey", "sk_test_123");
    com.example.health_care_system.service.TestUtils.injectField(realService, "serverPort", "8080");

        PaymentRequest req = new PaymentRequest(0L, 1L, "Free", "lkr");
        StripeService spyService = org.mockito.Mockito.spy(realService);
        com.stripe.model.checkout.Session mockSession = org.mockito.Mockito.mock(com.stripe.model.checkout.Session.class);
        org.mockito.Mockito.when(mockSession.getId()).thenReturn("sess_zero");
        org.mockito.Mockito.when(mockSession.getUrl()).thenReturn("https://checkout.stripe/sess_zero");
        org.mockito.Mockito.doReturn(mockSession).when(spyService).createSession(org.mockito.Mockito.any());

        var resp = spyService.checkoutProducts(req);
        assertNotNull(resp);
        assertEquals("SUCCESS", resp.getStatus());
    }

    @Test
    void checkoutProducts_missingSecretKey_throws() throws Exception {
        StripeService realService = new StripeService();
        // do not inject secretKey
        PaymentRequest req = new PaymentRequest(500L, 1L, "Consultation", "lkr");
        StripeService spyService = org.mockito.Mockito.spy(realService);
        org.mockito.Mockito.doThrow(new RuntimeException("Missing key")).when(spyService).createSession(org.mockito.Mockito.any());
        var resp = spyService.checkoutProducts(req);
        assertNotNull(resp);
        assertEquals("FAILED", resp.getStatus());
    }
}

