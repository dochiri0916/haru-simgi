package com.dochiri.jpa.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Converter(autoApply = true)
public class KstInstantAttributeConverter implements AttributeConverter<Instant, LocalDateTime> {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    public LocalDateTime convertToDatabaseColumn(Instant attribute) {
        if (attribute == null) {
            return null;
        }

        return LocalDateTime.ofInstant(attribute, KST);
    }

    @Override
    public Instant convertToEntityAttribute(LocalDateTime dbData) {
        if (dbData == null) {
            return null;
        }

        return dbData.atZone(KST).toInstant();
    }
}
