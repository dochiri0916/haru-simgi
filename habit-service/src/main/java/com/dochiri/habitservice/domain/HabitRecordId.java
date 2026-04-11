package com.dochiri.habitservice.domain;

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
            throw new InvalidHabitRecordIdException();
        }

        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidHabitRecordIdException();
        }
    }
}
