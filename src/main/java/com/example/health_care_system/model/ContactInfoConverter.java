package com.example.health_care_system.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ContactInfoConverter implements AttributeConverter<Hospital.ContactInfo, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Hospital.ContactInfo attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert ContactInfo to JSON", e);
        }
    }

    @Override
    public Hospital.ContactInfo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            return MAPPER.readValue(dbData, Hospital.ContactInfo.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read ContactInfo from JSON", e);
        }
    }
}
