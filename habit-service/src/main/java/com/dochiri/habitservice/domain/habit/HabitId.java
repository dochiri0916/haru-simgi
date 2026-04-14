package com.dochiri.habitservice.domain.habit;

import com.dochiri.habitservice.domain.habit.exception.InvalidHabitIdException;

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
            throw new InvalidHabitIdException(value);
        }

        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidHabitIdException(value);
        }
    }
}