package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.exception.InvalidHabitNameException;

public record HabitName(
        String value
) {
    public HabitName {
        validate(value);
    }

    public static HabitName of(String value) {
        return new HabitName(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank() || value.length() > 50) {
            throw new InvalidHabitNameException();
        }
    }
}