package com.example.health_care_system.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class AnalyticsPdfService {

    private static final DeviceRgb BLUE_COLOR = new DeviceRgb(59, 130, 246);
    private static final DeviceRgb GREEN_COLOR = new DeviceRgb(16, 185, 129);
    private static final DeviceRgb PURPLE_COLOR = new DeviceRgb(139, 92, 246);
    private static final DeviceRgb ORANGE_COLOR = new DeviceRgb(245, 158, 11);
    private static final DeviceRgb GRAY_COLOR = new DeviceRgb(107, 114, 128);
    private static final DeviceRgb RED_COLOR = new DeviceRgb(239, 68, 68);

    public byte[] generateAnalyticsReport(
            LocalDate startDate,
            LocalDate endDate,
            int totalAppointments,
            String completionRate,
            long uniqueDoctors,
            long uniquePatients,
            int scheduledCount,
            int completedCount,
            int cancelledCount,
            Map<String, Long> timeSlotData,
            Map<String, Long> dayOfWeekData,
            Map<String, Long> topDoctorsData,
            Map<String, Long> specializationData,
            Map<String, Long> statusData,
            Map<String, Long> monthlyData,
            Map<String, Long> peakHoursData
    ) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Set document margins
        document.setMargins(40, 40, 40, 40);

        // Title
        Paragraph title = new Paragraph("Healthcare System")
                .setFontSize(24)
                .setBold()
                .setFontColor(BLUE_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Reports & Analytics")
                .setFontSize(18)
                .setFontColor(GRAY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(subtitle);

        // Date Range
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        Paragraph dateRange = new Paragraph("Period: " + startDate.format(formatter) + " to " + endDate.format(formatter))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(dateRange);

        // Generated Date
        Paragraph generatedDate = new Paragraph("Generated on: " + LocalDate.now().format(formatter))
                .setFontSize(10)
                .setFontColor(GRAY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(generatedDate);

        // Summary Statistics Section
        document.add(new Paragraph("Summary Statistics")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10));

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                .useAllAvailableWidth();

        // Summary Cards
        summaryTable.addCell(createSummaryCell("Total Appointments", String.valueOf(totalAppointments), BLUE_COLOR));
        summaryTable.addCell(createSummaryCell("Completion Rate", completionRate + "%", GREEN_COLOR));
        summaryTable.addCell(createSummaryCell("Active Doctors", String.valueOf(uniqueDoctors), PURPLE_COLOR));
        summaryTable.addCell(createSummaryCell("Active Patients", String.valueOf(uniquePatients), ORANGE_COLOR));

        document.add(summaryTable);
        document.add(new Paragraph("\n"));

        // Detailed Statistics
        document.add(new Paragraph("Detailed Statistics")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10));

        Table detailTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth();

        detailTable.addCell(createDetailCell("Scheduled", String.valueOf(scheduledCount), BLUE_COLOR));
        detailTable.addCell(createDetailCell("Completed", String.valueOf(completedCount), GREEN_COLOR));
        detailTable.addCell(createDetailCell("Cancelled", String.valueOf(cancelledCount), RED_COLOR));

        document.add(detailTable);
        document.add(new Paragraph("\n"));

        // Time Slot Analysis
        document.add(new Paragraph("Most Booked Time Slots")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        document.add(createDataTable(timeSlotData, "Time Slot", "Appointments"));
        document.add(new Paragraph("\n"));

        // Day of Week Analysis
        document.add(new Paragraph("Appointments by Day of Week")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        document.add(createDataTable(dayOfWeekData, "Day", "Appointments"));
        document.add(new Paragraph("\n"));

        // Page break for next section
        document.add(new AreaBreak());

        // Top Doctors
        document.add(new Paragraph("Top Doctors by Appointments")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        document.add(createDataTable(topDoctorsData, "Doctor", "Appointments"));
        document.add(new Paragraph("\n"));

        // Specialization Demand
        document.add(new Paragraph("Specialization Demand")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        document.add(createDataTableWithPercentage(specializationData, "Specialization", "Appointments"));
        document.add(new Paragraph("\n"));

        // Peak Hours
        document.add(new Paragraph("Peak Hours Analysis")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        document.add(createDataTable(peakHoursData, "Hour", "Appointments"));
        document.add(new Paragraph("\n"));

        // Status Distribution
        document.add(new Paragraph("Appointment Status Distribution")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        document.add(createDataTableWithPercentage(statusData, "Status", "Count"));
        document.add(new Paragraph("\n"));

        // Monthly Trend
        document.add(new Paragraph("6-Month Trend")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        document.add(createDataTable(monthlyData, "Month", "Appointments"));

        // Footer
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("This report is generated automatically by Healthcare System Analytics Module")
                .setFontSize(9)
                .setFontColor(GRAY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    private Cell createSummaryCell(String label, String value, DeviceRgb color) {
        Cell cell = new Cell();
        cell.add(new Paragraph(label)
                .setFontSize(10)
                .setFontColor(GRAY_COLOR)
                .setMarginBottom(5));
        cell.add(new Paragraph(value)
                .setFontSize(18)
                .setBold()
                .setFontColor(color));
        cell.setPadding(10);
        cell.setBackgroundColor(new DeviceRgb(249, 250, 251));
        return cell;
    }

    private Cell createDetailCell(String label, String value, DeviceRgb color) {
        Cell cell = new Cell();
        cell.add(new Paragraph(label)
                .setFontSize(11)
                .setFontColor(GRAY_COLOR)
                .setMarginBottom(5));
        cell.add(new Paragraph(value)
                .setFontSize(16)
                .setBold()
                .setFontColor(ColorConstants.BLACK));
        cell.setPadding(10);
        cell.setBorderLeft(new com.itextpdf.layout.borders.SolidBorder(color, 3));
        return cell;
    }

    private Table createDataTable(Map<String, Long> data, String col1Header, String col2Header) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth();

        // Header
        table.addHeaderCell(new Cell()
                .add(new Paragraph(col1Header).setBold())
                .setBackgroundColor(new DeviceRgb(243, 244, 246))
                .setPadding(8));
        table.addHeaderCell(new Cell()
                .add(new Paragraph(col2Header).setBold())
                .setBackgroundColor(new DeviceRgb(243, 244, 246))
                .setPadding(8)
                .setTextAlignment(TextAlignment.RIGHT));

        // Data rows
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            table.addCell(new Cell().add(new Paragraph(entry.getKey())).setPadding(6));
            table.addCell(new Cell()
                    .add(new Paragraph(String.valueOf(entry.getValue())))
                    .setPadding(6)
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        return table;
    }

    private Table createDataTableWithPercentage(Map<String, Long> data, String col1Header, String col2Header) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1}))
                .useAllAvailableWidth();

        // Calculate total
        long total = data.values().stream().mapToLong(Long::longValue).sum();

        // Header
        table.addHeaderCell(new Cell()
                .add(new Paragraph(col1Header).setBold())
                .setBackgroundColor(new DeviceRgb(243, 244, 246))
                .setPadding(8));
        table.addHeaderCell(new Cell()
                .add(new Paragraph(col2Header).setBold())
                .setBackgroundColor(new DeviceRgb(243, 244, 246))
                .setPadding(8)
                .setTextAlignment(TextAlignment.RIGHT));
        table.addHeaderCell(new Cell()
                .add(new Paragraph("Percentage").setBold())
                .setBackgroundColor(new DeviceRgb(243, 244, 246))
                .setPadding(8)
                .setTextAlignment(TextAlignment.RIGHT));

        // Data rows
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            double percentage = total > 0 ? (entry.getValue() * 100.0 / total) : 0;
            
            table.addCell(new Cell().add(new Paragraph(entry.getKey())).setPadding(6));
            table.addCell(new Cell()
                    .add(new Paragraph(String.valueOf(entry.getValue())))
                    .setPadding(6)
                    .setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell()
                    .add(new Paragraph(String.format("%.1f%%", percentage)))
                    .setPadding(6)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(GRAY_COLOR));
        }

        return table;
    }
}
