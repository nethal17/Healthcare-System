package com.example.health_care_system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notification_history")
public class NotificationHistory {

    @Id
    private String id; // UUID string

    @Column(name = "appointment_id")
    private String appointmentId;

    @Column(name = "patient_id")
    private String patientId;

    @Column(name = "doctor_id")
    private String doctorId;

    @Column(name = "channel")
    private String channel; // WHATSAPP or SMS

    @Column(name = "status")
    private String status; // SENT or FAILED

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "error", length = 2000)
    private String error;

    public NotificationHistory() {
    }

    public NotificationHistory(String id, String appointmentId, String patientId, String doctorId, String channel, String status, Instant sentAt, String message, String error) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.channel = channel;
        this.status = status;
        this.sentAt = sentAt;
        this.message = message;
        this.error = error;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
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
