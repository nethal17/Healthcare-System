package com.example.health_care_system.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ReportDataConverter implements AttributeConverter<AnalyticsReport.ReportData, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(AnalyticsReport.ReportData attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert ReportData to JSON", e);
        }
    }

    @Override
    public AnalyticsReport.ReportData convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            return MAPPER.readValue(dbData, AnalyticsReport.ReportData.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read ReportData from JSON", e);
        }
    }
}
