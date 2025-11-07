package com.example.health_care_system.service.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

// NOTE: WhatsApp sender disabled — no @Service annotation so it's not registered as a bean.
public class WhatsAppNotificationService implements NotificationService {

    private final RestTemplate restTemplate;
    private final String whatsappApiUrl;
    private final String whatsappToken;
    private final boolean enabled;

    public WhatsAppNotificationService(RestTemplate restTemplate,
                                      @Value("${notifications.whatsapp.api.url:}") String whatsappApiUrl,
                                      @Value("${notifications.whatsapp.api.token:}") String whatsappToken,
                                      @Value("${notifications.enabled:false}") boolean enabled) {
        this.restTemplate = restTemplate;
        this.whatsappApiUrl = whatsappApiUrl;
        this.whatsappToken = whatsappToken;
        this.enabled = enabled;
    }

    @Override
    public NotificationResult send(NotificationRequest request) {
        if (!enabled || whatsappApiUrl == null || whatsappApiUrl.isBlank()) {
            return new NotificationResult(NotificationResult.Status.FAILED, null, "WhatsApp not configured or disabled");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(whatsappToken == null ? "" : whatsappToken);

            Map<String, Object> payload = new HashMap<>();
            // Example payload - adapt to your WhatsApp cloud request structure
            payload.put("to", request.getWhatsappNumber());
            payload.put("type", "text");
            Map<String, String> text = new HashMap<>();
            text.put("body", request.getMessage());
            payload.put("text", text);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(whatsappApiUrl, entity, String.class);

            return new NotificationResult(NotificationResult.Status.SENT, "WhatsApp sent", null);
        } catch (Exception ex) {
            return new NotificationResult(NotificationResult.Status.FAILED, null, ex.getMessage());
        }
    }
}
