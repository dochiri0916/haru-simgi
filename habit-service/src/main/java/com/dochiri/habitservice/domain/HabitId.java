package com.dochiri.habitservice.domain;

import java.util.UUID;

public record HabitId(
        String value
) {
    public HabitId {
        validate(value);
    }

    public static HabitId newId() {
        return new HabitId(UUID.randomUUID().toString());
    }

    public static HabitId of(String value) {
        return new HabitId(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidHabitIdException();
        }

        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidHabitIdException();
        }
    }
}