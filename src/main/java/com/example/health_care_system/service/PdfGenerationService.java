package com.example.health_care_system.service;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Hospital;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.Payment;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerationService {

    /**
     * Generate appointment confirmation PDF
     */
    public byte[] generateAppointmentConfirmationPdf(
            Appointment appointment,
            Patient patient,
            Doctor doctor,
            Hospital hospital,
            Payment payment) {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Define colors
            DeviceRgb primaryBlue = new DeviceRgb(37, 99, 235);
            DeviceRgb lightGray = new DeviceRgb(249, 250, 251);
            DeviceRgb darkGray = new DeviceRgb(55, 65, 81);
            DeviceRgb successGreen = new DeviceRgb(34, 197, 94);
            
            // Header
            Paragraph header = new Paragraph("APPOINTMENT CONFIRMATION")
                .setFontSize(24)
                .setBold()
                .setFontColor(primaryBlue)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
            document.add(header);
            
            Paragraph subHeader = new Paragraph("Healthcare System - Sri Lanka")
                .setFontSize(12)
                .setFontColor(darkGray)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(subHeader);
            
            // Confirmation badge
            Paragraph confirmed = new Paragraph("✓ CONFIRMED")
                .setFontSize(14)
                .setBold()
                .setFontColor(successGreen)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(confirmed);
            
            // Appointment ID
            Paragraph appointmentId = new Paragraph("Appointment ID: " + appointment.getId())
                .setFontSize(10)
                .setFontColor(darkGray)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(appointmentId);
            
            // Patient Information Section
            document.add(createSectionHeader("Patient Information", primaryBlue));
            Table patientTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);
            
            addTableRow(patientTable, "Patient Name:", patient.getName(), lightGray);
            addTableRow(patientTable, "Patient ID:", patient.getId(), ColorConstants.WHITE);
            addTableRow(patientTable, "Email:", patient.getEmail(), lightGray);
            addTableRow(patientTable, "Contact Number:", patient.getContactNumber() != null ? patient.getContactNumber() : "N/A", ColorConstants.WHITE);
            
            document.add(patientTable);
            
            // Hospital Information Section
            document.add(createSectionHeader("Hospital Information", primaryBlue));
            Table hospitalTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);
            
            addTableRow(hospitalTable, "Hospital Name:", hospital.getName(), lightGray);
            addTableRow(hospitalTable, "Hospital Type:", hospital.getType().toString(), ColorConstants.WHITE);
            if (hospital.getLocation() != null) {
                String address = hospital.getLocation().getAddress() + ", " + 
                               hospital.getLocation().getCity() + ", " + 
                               hospital.getLocation().getState();
                addTableRow(hospitalTable, "Address:", address, lightGray);
            }
            if (hospital.getContactInfo() != null && hospital.getContactInfo().getPhoneNumber() != null) {
                addTableRow(hospitalTable, "Phone:", hospital.getContactInfo().getPhoneNumber(), ColorConstants.WHITE);
            }
            
            document.add(hospitalTable);
            
            // Doctor Information Section
            document.add(createSectionHeader("Doctor Information", primaryBlue));
            Table doctorTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);
            
            addTableRow(doctorTable, "Doctor Name:", "Dr. " + doctor.getName(), lightGray);
            addTableRow(doctorTable, "Specialization:", doctor.getSpecialization(), ColorConstants.WHITE);
            addTableRow(doctorTable, "Doctor ID:", doctor.getId(), lightGray);
            addTableRow(doctorTable, "Email:", doctor.getEmail(), ColorConstants.WHITE);
            
            document.add(doctorTable);
            
            // Appointment Details Section
            document.add(createSectionHeader("Appointment Details", primaryBlue));
            Table appointmentTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
            
            addTableRow(appointmentTable, "Date:", appointment.getAppointmentDateTime().format(dateFormatter), lightGray);
            addTableRow(appointmentTable, "Time:", appointment.getAppointmentDateTime().format(timeFormatter), ColorConstants.WHITE);
            addTableRow(appointmentTable, "Status:", appointment.getStatus().toString(), lightGray);
            
            if (appointment.getPurpose() != null && !appointment.getPurpose().isEmpty()) {
                addTableRow(appointmentTable, "Purpose:", appointment.getPurpose(), ColorConstants.WHITE);
            }
            
            document.add(appointmentTable);
            
            // Payment Information
            document.add(createSectionHeader("Payment Information", primaryBlue));
            Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);
            
            if (hospital.getType() == Hospital.HospitalType.GOVERNMENT) {
                addTableRow(paymentTable, "Consultation Fee:", "FREE (Government Hospital)", successGreen, true);
            } else {
                String fee = "Rs. " + hospital.getHospitalCharges();
                addTableRow(paymentTable, "Consultation Fee:", fee, lightGray);
                
                // Add detailed payment information for CARD payments only
                if (payment != null && payment.getPaymentMethod() == Payment.PaymentMethod.CARD) {
                    addTableRow(paymentTable, "Payment Method:", "Card Payment (Stripe)", ColorConstants.WHITE);
                    addTableRow(paymentTable, "Payment Amount:", "Rs. " + payment.getAmount(), lightGray);
                    
                    String paymentStatus = payment.getStatus().toString();
                    if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
                        addTableRow(paymentTable, "Payment Status:", paymentStatus, successGreen, true);
                        
                        // Add payment success confirmation message
                        Cell confirmationLabelCell = new Cell()
                            .add(new Paragraph("Confirmation:").setFontSize(10).setBold())
                            .setBackgroundColor(ColorConstants.WHITE)
                            .setPadding(8);
                        
                        Cell confirmationValueCell = new Cell()
                            .add(new Paragraph("✓ Payment Successfully Completed - Your appointment has been confirmed and paid.")
                                .setFontSize(10)
                                .setFontColor(successGreen)
                                .setBold())
                            .setBackgroundColor(ColorConstants.WHITE)
                            .setPadding(8);
                        
                        paymentTable.addCell(confirmationLabelCell);
                        paymentTable.addCell(confirmationValueCell);
                    } else {
                        addTableRow(paymentTable, "Payment Status:", paymentStatus, ColorConstants.WHITE);
                    }
                    
                    // Add transaction ID if available
                    if (payment.getTransactionId() != null && !payment.getTransactionId().isEmpty()) {
                        addTableRow(paymentTable, "Transaction ID:", payment.getTransactionId(), lightGray);
                    }
                }
            }
            
            document.add(paymentTable);
            
            // Important Notes
            document.add(createSectionHeader("Important Notes", new DeviceRgb(234, 179, 8)));
            Paragraph notes = new Paragraph()
                .setFontSize(10)
                .setMarginBottom(10);
            
            notes.add("• Please arrive 15 minutes before your scheduled appointment time.\n");
            notes.add("• Bring your ID card and this confirmation document.\n");
            notes.add("• If you need to cancel or reschedule, please do so at least 24 hours in advance.\n");
            
            if (hospital.getType() == Hospital.HospitalType.PRIVATE) {
                notes.add("• Please bring the required payment method for consultation fees.\n");
            }
            
            notes.add("• For any queries, contact the hospital directly.\n");
            
            document.add(notes);
            
            // Footer
            Paragraph footer = new Paragraph("\nThis is a computer-generated document. No signature is required.")
                .setFontSize(8)
                .setFontColor(darkGray)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
            document.add(footer);
            
            Paragraph generatedDate = new Paragraph("Generated on: " + 
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' h:mm a")))
                .setFontSize(8)
                .setFontColor(darkGray)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(generatedDate);
            
            document.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Create a section header
     */
    private Paragraph createSectionHeader(String title, DeviceRgb color) {
        return new Paragraph(title)
            .setFontSize(14)
            .setBold()
            .setFontColor(color)
            .setMarginTop(10)
            .setMarginBottom(8);
    }
    
    /**
     * Add a row to a table with alternating background colors
     */
    private void addTableRow(Table table, String label, String value, DeviceRgb backgroundColor) {
        addTableRow(table, label, value, backgroundColor, false);
    }
    
    /**
     * Add a row to a table with custom styling
     */
    private void addTableRow(Table table, String label, String value, DeviceRgb backgroundColor, boolean bold) {
        Cell labelCell = new Cell()
            .add(new Paragraph(label).setFontSize(10).setBold())
            .setBackgroundColor(backgroundColor)
            .setPadding(8);
        
        Paragraph valueParagraph = new Paragraph(value).setFontSize(10);
        if (bold) {
            valueParagraph.setBold();
        }
        
        Cell valueCell = new Cell()
            .add(valueParagraph)
            .setBackgroundColor(backgroundColor)
            .setPadding(8);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    /**
     * Helper method to add table row with ColorConstants (overload)
     */
    private void addTableRow(Table table, String label, String value, com.itextpdf.kernel.colors.Color backgroundColor) {
        Cell labelCell = new Cell()
            .add(new Paragraph(label).setFontSize(10).setBold())
            .setBackgroundColor(backgroundColor)
            .setPadding(8);
        
        Cell valueCell = new Cell()
            .add(new Paragraph(value).setFontSize(10))
            .setBackgroundColor(backgroundColor)
            .setPadding(8);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
