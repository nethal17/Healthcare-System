package com.example.health_care_system.service.notification;

public class NotificationResult {
    public enum Status { SENT, FAILED }

    private Status status;
    private String message;
    private String error;

    public NotificationResult() {}

    public NotificationResult(Status status, String message, String error) {
        this.status = status;
        this.message = message;
        this.error = error;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
