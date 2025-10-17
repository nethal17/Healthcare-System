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

    /**
     * Generate insurance appointment confirmation PDF with comprehensive details
     */
    public byte[] generateInsuranceAppointmentPdf(
            Appointment appointment,
            Patient patient,
            Doctor doctor,
            Hospital hospital,
            String insuranceProvider,
            String policyNumber
    ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            document.setMargins(30, 30, 30, 30);

            // Color scheme
            DeviceRgb primaryBlue = new DeviceRgb(30, 64, 175);
            DeviceRgb accentGreen = new DeviceRgb(16, 185, 129);
            DeviceRgb amberColor = new DeviceRgb(245, 158, 11);
            DeviceRgb lightGray = new DeviceRgb(243, 244, 246);
            DeviceRgb darkGray = new DeviceRgb(31, 41, 55);
            DeviceRgb white = new DeviceRgb(255, 255, 255);

            // ========== HEADER ==========
            Paragraph mainHeader = new Paragraph("HEALTHCARE SYSTEM")
                    .setFontSize(26)
                    .setBold()
                    .setFontColor(primaryBlue)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(3);
            document.add(mainHeader);

            Paragraph subHeader = new Paragraph("Smart Healthcare Management")
                    .setFontSize(11)
                    .setFontColor(primaryBlue)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(subHeader);

            // Horizontal line
            Table headerLine = new Table(1).setWidth(UnitValue.createPercentValue(100));
            headerLine.addCell(new Cell()
                    .add(new Paragraph(""))
                    .setHeight(3)
                    .setBackgroundColor(primaryBlue)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            document.add(headerLine);

            document.add(new Paragraph("\n"));

            // ========== TITLE ==========
            Paragraph title = new Paragraph("Appointment Confirmation with Insurance Claim")
                    .setFontSize(18)
                    .setBold()
                    .setFontColor(darkGray)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(title);

            // ========== STATUS BADGE ==========
            Table statusBadge = new Table(1).setWidth(UnitValue.createPercentValue(60))
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            Cell statusCell = new Cell()
                    .add(new Paragraph("⏳ INSURANCE CLAIM PENDING")
                            .setFontSize(12)
                            .setBold()
                            .setFontColor(white)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(amberColor)
                    .setPadding(10)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
            statusBadge.addCell(statusCell);
            document.add(statusBadge);

            document.add(new Paragraph("\n"));

            // ========== PATIENT INFORMATION ==========
            Paragraph patientTitle = new Paragraph("Patient Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(primaryBlue)
                    .setMarginBottom(8);
            document.add(patientTitle);

            Table patientTable = new Table(UnitValue.createPercentArray(new float[]{35f, 65f}))
                    .setWidth(UnitValue.createPercentValue(100));
            
            addStyledTableRow(patientTable, "Patient ID:", patient.getId(), lightGray);
            addStyledTableRow(patientTable, "Full Name:", patient.getName(), white);
            addStyledTableRow(patientTable, "Date of Birth:", 
                    patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : "N/A", lightGray);
            addStyledTableRow(patientTable, "Gender:", 
                    patient.getGender() != null ? patient.getGender() : "N/A", white);
            addStyledTableRow(patientTable, "Contact Number:", 
                    patient.getContactNumber() != null ? patient.getContactNumber() : "N/A", lightGray);
            addStyledTableRow(patientTable, "Email:", 
                    patient.getEmail() != null ? patient.getEmail() : "N/A", white);
            addStyledTableRow(patientTable, "Address:", 
                    patient.getAddress() != null ? patient.getAddress() : "N/A", lightGray);
            
            document.add(patientTable);
            document.add(new Paragraph("\n"));

            // ========== HOSPITAL INFORMATION ==========
            Paragraph hospitalTitle = new Paragraph("Hospital Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(primaryBlue)
                    .setMarginBottom(8);
            document.add(hospitalTitle);

            if (hospital != null) {
                Table hospitalTable = new Table(UnitValue.createPercentArray(new float[]{35f, 65f}))
                        .setWidth(UnitValue.createPercentValue(100));
                
                addStyledTableRow(hospitalTable, "Hospital Name:", hospital.getName(), lightGray);
                addStyledTableRow(hospitalTable, "Address:", 
                        hospital.getLocation() != null && hospital.getLocation().getAddress() != null ? 
                                hospital.getLocation().getAddress() : "N/A", white);
                addStyledTableRow(hospitalTable, "Contact:", 
                        hospital.getContactInfo() != null && hospital.getContactInfo().getPhoneNumber() != null ? 
                                hospital.getContactInfo().getPhoneNumber() : "N/A", lightGray);
                addStyledTableRow(hospitalTable, "Type:", 
                        hospital.getType() != null ? hospital.getType().toString() : "N/A", white);
                
                document.add(hospitalTable);
            } else {
                document.add(new Paragraph("Hospital information not available").setItalic().setFontSize(10));
            }
            
            document.add(new Paragraph("\n"));

            // ========== DOCTOR INFORMATION ==========
            Paragraph doctorTitle = new Paragraph("Doctor Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(primaryBlue)
                    .setMarginBottom(8);
            document.add(doctorTitle);

            if (doctor != null) {
                Table doctorTable = new Table(UnitValue.createPercentArray(new float[]{35f, 65f}))
                        .setWidth(UnitValue.createPercentValue(100));
                
                addStyledTableRow(doctorTable, "Doctor Name:", "Dr. " + doctor.getName(), lightGray);
                addStyledTableRow(doctorTable, "Specialization:", 
                        doctor.getSpecialization() != null ? doctor.getSpecialization() : "N/A", white);
                addStyledTableRow(doctorTable, "Contact:", 
                        doctor.getContactNumber() != null ? doctor.getContactNumber() : "N/A", lightGray);
                
                document.add(doctorTable);
            } else {
                document.add(new Paragraph("Doctor information not available").setItalic().setFontSize(10));
            }
            
            document.add(new Paragraph("\n"));

            // ========== APPOINTMENT DETAILS ==========
            Paragraph appointmentTitle = new Paragraph("Appointment Details")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(primaryBlue)
                    .setMarginBottom(8);
            document.add(appointmentTitle);

            Table appointmentTable = new Table(UnitValue.createPercentArray(new float[]{35f, 65f}))
                    .setWidth(UnitValue.createPercentValue(100));
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

            addStyledTableRow(appointmentTable, "Appointment ID:", appointment.getId(), lightGray);
            addStyledTableRow(appointmentTable, "Date:", 
                    appointment.getAppointmentDateTime() != null ? 
                            appointment.getAppointmentDateTime().format(dateFormatter) : "N/A", white);
            addStyledTableRow(appointmentTable, "Time:", 
                    appointment.getAppointmentDateTime() != null ? 
                            appointment.getAppointmentDateTime().format(timeFormatter) : "N/A", lightGray);
            addStyledTableRow(appointmentTable, "Status:", 
                    appointment.getStatus() != null ? appointment.getStatus().toString() : "SCHEDULED", white);
            addStyledTableRow(appointmentTable, "Purpose:", 
                    appointment.getPurpose() != null ? appointment.getPurpose() : "General Consultation", lightGray);
            
            document.add(appointmentTable);
            document.add(new Paragraph("\n"));

            // ========== INSURANCE INFORMATION ==========
            Paragraph insuranceTitle = new Paragraph("Insurance Information")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(primaryBlue)
                    .setMarginBottom(8);
            document.add(insuranceTitle);

            Table insuranceTable = new Table(UnitValue.createPercentArray(new float[]{35f, 65f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBackgroundColor(new DeviceRgb(243, 232, 255)); // Light purple highlight
            
            Cell insuranceLabel1 = new Cell()
                    .add(new Paragraph("Insurance Provider:").setFontSize(10).setBold())
                    .setPadding(10);
            Cell insuranceValue1 = new Cell()
                    .add(new Paragraph(insuranceProvider != null ? insuranceProvider : "N/A").setFontSize(10))
                    .setPadding(10);
            insuranceTable.addCell(insuranceLabel1);
            insuranceTable.addCell(insuranceValue1);

            Cell insuranceLabel2 = new Cell()
                    .add(new Paragraph("Policy Number:").setFontSize(10).setBold())
                    .setPadding(10);
            Cell insuranceValue2 = new Cell()
                    .add(new Paragraph(policyNumber != null ? policyNumber : "N/A").setFontSize(10))
                    .setPadding(10);
            insuranceTable.addCell(insuranceLabel2);
            insuranceTable.addCell(insuranceValue2);

            Cell insuranceLabel3 = new Cell()
                    .add(new Paragraph("Claim Status:").setFontSize(10).setBold())
                    .setPadding(10);
            Cell insuranceValue3 = new Cell()
                    .add(new Paragraph("PENDING VERIFICATION").setFontSize(10).setBold().setFontColor(amberColor))
                    .setPadding(10);
            insuranceTable.addCell(insuranceLabel3);
            insuranceTable.addCell(insuranceValue3);
            
            document.add(insuranceTable);
            document.add(new Paragraph("\n"));

            // ========== CONFIRMATION MESSAGE ==========
            Table confirmBox = new Table(1).setWidth(UnitValue.createPercentValue(100));
            Cell confirmCell = new Cell()
                    .add(new Paragraph("✓ APPOINTMENT CONFIRMED")
                            .setFontSize(14)
                            .setBold()
                            .setFontColor(white)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(5))
                    .add(new Paragraph("Your appointment has been successfully booked. Your insurance claim is currently under review and you will be contacted if additional information is needed.")
                            .setFontSize(10)
                            .setFontColor(white)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(accentGreen)
                    .setPadding(15)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
            confirmBox.addCell(confirmCell);
            document.add(confirmBox);

            document.add(new Paragraph("\n"));

            // ========== IMPORTANT INSTRUCTIONS ==========
            Paragraph instructionTitle = new Paragraph("Important Instructions")
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(primaryBlue)
                    .setMarginBottom(8);
            document.add(instructionTitle);

            Table instructionBox = new Table(1).setWidth(UnitValue.createPercentValue(100));
            Cell instructionCell = new Cell()
                    .add(new Paragraph("• Please arrive 15 minutes before your scheduled appointment time.")
                            .setFontSize(9)
                            .setMarginBottom(4))
                    .add(new Paragraph("• Bring a valid photo ID and your insurance card.")
                            .setFontSize(9)
                            .setMarginBottom(4))
                    .add(new Paragraph("• Your insurance claim is being verified. You will be contacted if additional information is needed.")
                            .setFontSize(9)
                            .setMarginBottom(4))
                    .add(new Paragraph("• If you need to reschedule or cancel, please contact us at least 24 hours in advance.")
                            .setFontSize(9)
                            .setMarginBottom(4))
                    .add(new Paragraph("• Keep this document for your records and bring it to your appointment.")
                            .setFontSize(9))
                    .setBackgroundColor(lightGray)
                    .setPadding(12)
                    .setBorder(new com.itextpdf.layout.borders.SolidBorder(primaryBlue, 1));
            instructionBox.addCell(instructionCell);
            document.add(instructionBox);

            document.add(new Paragraph("\n\n"));

            // ========== FOOTER ==========
            Table footerLine = new Table(1).setWidth(UnitValue.createPercentValue(100));
            footerLine.addCell(new Cell()
                    .add(new Paragraph(""))
                    .setHeight(1)
                    .setBackgroundColor(lightGray)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            document.add(footerLine);

            Paragraph footer = new Paragraph(
                    "Healthcare System - Smart Healthcare Management\n" +
                    "For inquiries: support@healthcare.com | Phone: 1-800-HEALTH-CARE\n" +
                    "This is a computer-generated document and does not require a signature.")
                    .setFontSize(8)
                    .setFontColor(new DeviceRgb(107, 114, 128))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    /**
     * Helper method to add styled table rows with alternating colors
     */
    private void addStyledTableRow(Table table, String label, String value, DeviceRgb backgroundColor) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFontSize(10).setBold())
                .setBackgroundColor(backgroundColor)
                .setPadding(8)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(new DeviceRgb(229, 231, 235), 0.5f));

        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "N/A").setFontSize(10))
                .setBackgroundColor(backgroundColor)
                .setPadding(8)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(new DeviceRgb(229, 231, 235), 0.5f));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
