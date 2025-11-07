package com.example.health_care_system.service.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service("smsNotificationService")
public class SmsNotificationService implements NotificationService {

    private final RestTemplate restTemplate;
    private final String smsApiUrl;
    private final String smsApiKey;
    private final String smsSenderId;
    private final boolean enabled;

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);

    public SmsNotificationService(RestTemplate restTemplate,
                                  @Value("${notifications.sms.api.url:}") String smsApiUrl,
                                  @Value("${notifications.sms.api.key:}") String smsApiKey,
                                  @Value("${notifications.sms.sender-id:TextLKDemo}") String smsSenderId,
                                  @Value("${notifications.enabled:false}") boolean enabled) {
        this.restTemplate = restTemplate;
        this.smsApiUrl = smsApiUrl;
        this.smsApiKey = smsApiKey;
        this.smsSenderId = smsSenderId;
        this.enabled = enabled;
    }

    @Override
    public NotificationResult send(NotificationRequest request) {
        if (!enabled || smsApiUrl == null || smsApiUrl.isBlank()) {
            return new NotificationResult(NotificationResult.Status.FAILED, null, "SMS not configured or disabled");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            logger.debug("Sending SMS via url={} with senderId={}", smsApiUrl, smsSenderId);
            // Text.lk has two API variants:
            // - Older HTTP API (/api/http/sms/send) expects api_token inside the JSON body
            // - Newer v3 API (/api/v3/sms/send) expects Authorization: Bearer <token>
            boolean useHttpApiTokenInBody = smsApiUrl != null && smsApiUrl.contains("/api/http/");
            if (!useHttpApiTokenInBody) {
                // v3: Authorization: Bearer <token>
                headers.setBearerAuth(smsApiKey == null ? "" : smsApiKey);
            }

            String recipient = request.getPhoneNumber();
            if (recipient == null || recipient.isBlank()) {
                return new NotificationResult(NotificationResult.Status.FAILED, null, "No recipient phone number");
            }

            Map<String, Object> payload = new HashMap<>();
            // Text.lk expects number without +, example: 94710000000
            payload.put("recipient", recipient.replaceAll("\\+", ""));
            payload.put("sender_id", smsSenderId == null || smsSenderId.isBlank() ? "TextLKDemo" : smsSenderId);
            payload.put("type", "plain");
            payload.put("message", request.getMessage());
            // If using older HTTP API, include api_token in the body
            if (useHttpApiTokenInBody && smsApiKey != null && !smsApiKey.isBlank()) {
                payload.put("api_token", smsApiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> resp = null;
            try {
                resp = restTemplate.exchange(smsApiUrl, HttpMethod.POST, entity, String.class);
            } catch (HttpClientErrorException httpEx) {
                String respBody = httpEx.getResponseBodyAsString();
                logger.warn("SMS API returned error status {}: {}", httpEx.getStatusCode(), respBody);
                return new NotificationResult(NotificationResult.Status.FAILED, null, "HTTP " + httpEx.getStatusCode() + ": " + respBody);
            }

            String body = resp.getBody();
            int status = resp.getStatusCodeValue();
            logger.info("SMS API responded with status {} and body: {}", status, body);

            if (status >= 200 && status < 300) {
                // Heuristic: provider might include 'success' in body, but consider 2xx as success
                return new NotificationResult(NotificationResult.Status.SENT, "SMS sent", null);
            }

            return new NotificationResult(NotificationResult.Status.FAILED, null, "HTTP " + status + ": " + (body == null ? "empty" : body));
        } catch (Exception ex) {
            logger.error("Exception when sending SMS", ex);
            return new NotificationResult(NotificationResult.Status.FAILED, null, ex.getMessage());
        }
    }
}
