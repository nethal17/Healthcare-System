package com.example.health_care_system.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service("emailNotificationService")
public class EmailNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final boolean enabled;

    public EmailNotificationService(JavaMailSender mailSender,
                                    @Value("${spring.mail.username:}") String fromEmail,
                                    @Value("${notifications.email.enabled:false}") boolean enabled) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.enabled = enabled;
    }

    @Override
    public NotificationResult send(NotificationRequest request) {
        if (!enabled) {
            return new NotificationResult(NotificationResult.Status.FAILED, null, "Email notifications disabled");
        }

        String to = request.getEmailAddress();
        if (to == null || to.isBlank()) {
            return new NotificationResult(NotificationResult.Status.FAILED, null, "No email address provided");
        }

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(fromEmail == null || fromEmail.isBlank() ? "no-reply@example.com" : fromEmail);
            helper.setTo(to);
            helper.setSubject("Clinic notification");
            helper.setText(request.getMessage() == null ? "" : request.getMessage(), false);
            mailSender.send(msg);
            return new NotificationResult(NotificationResult.Status.SENT, "Email queued", null);
        } catch (MessagingException mex) {
            logger.warn("Failed to create email message", mex);
            return new NotificationResult(NotificationResult.Status.FAILED, null, mex.getMessage());
        } catch (Exception ex) {
            logger.error("Failed to send email notification", ex);
            return new NotificationResult(NotificationResult.Status.FAILED, null, ex.getMessage());
        }
    }
}

