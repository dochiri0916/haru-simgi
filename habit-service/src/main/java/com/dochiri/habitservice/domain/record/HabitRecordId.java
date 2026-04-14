package com.dochiri.habitservice.domain.record;

import com.dochiri.habitservice.domain.record.exception.InvalidHabitRecordIdException;

import java.util.UUID;

public record HabitRecordId(
        String value
) {
    public HabitRecordId {
        validate(value);
    }

    public static HabitRecordId newId() {
        return new HabitRecordId(UUID.randomUUID().toString());
    }

    public static HabitRecordId of(String value) {
        return new HabitRecordId(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidHabitRecordIdException(value);
        }

        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidHabitRecordIdException(value);
        }
    }
}