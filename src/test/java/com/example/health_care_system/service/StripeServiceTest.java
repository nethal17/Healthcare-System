package com.example.health_care_system.service;

import com.example.health_care_system.dto.PaymentRequest;
import com.example.health_care_system.dto.StripeResponse;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class StripeServiceTest {

    @Test
    void checkoutProducts_success() throws Exception {
        StripeService realService = new StripeService();
        // inject secretKey and serverPort
        TestUtils.injectField(realService, "secretKey", "sk_test_123");
        TestUtils.injectField(realService, "serverPort", "8080");

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
        TestUtils.injectField(realService, "secretKey", "sk_test_123");
        TestUtils.injectField(realService, "serverPort", "8080");

        PaymentRequest req = new PaymentRequest(500L, 1L, "Consultation", "lkr");

        StripeService spyService = Mockito.spy(realService);
        Mockito.doThrow(new RuntimeException("Stripe down")).when(spyService).createSession(Mockito.any());

        var resp = spyService.checkoutProducts(req);
        assertNotNull(resp);
        assertEquals("FAILED", resp.getStatus());
    }
}
