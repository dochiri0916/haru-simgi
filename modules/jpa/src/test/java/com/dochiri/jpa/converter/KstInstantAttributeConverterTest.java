package com.dochiri.jpa.converter;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class KstInstantAttributeConverterTest {

    private final KstInstantAttributeConverter converter = new KstInstantAttributeConverter();

    @Test
    void instant를_DB에_저장할_때_KST_LocalDateTime으로_변환한다() {
        Instant instant = Instant.parse("2026-04-15T16:00:36.345146Z");

        LocalDateTime dbValue = converter.convertToDatabaseColumn(instant);

        assertThat(dbValue).isEqualTo(LocalDateTime.parse("2026-04-16T01:00:36.345146"));
    }

    @Test
    void DB의_KST_LocalDateTime을_읽을_때_동일한_Instant로_복원한다() {
        LocalDateTime dbValue = LocalDateTime.parse("2026-04-16T01:00:36.345146");

        Instant instant = converter.convertToEntityAttribute(dbValue);

        assertThat(instant).isEqualTo(Instant.parse("2026-04-15T16:00:36.345146Z"));
    }
}
