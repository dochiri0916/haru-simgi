package com.dochiri.habitservice.domain.habit;

import com.dochiri.habitservice.domain.habit.exception.InvalidHabitIndexException;

public record HabitIndex(
        int value
) {
    public HabitIndex {
        validate(value);
    }

    public static HabitIndex of(int value) {
        return new HabitIndex(value);
    }

    private static void validate(int value) {
        if (value < 0) {
            throw new InvalidHabitIndexException(value);
        }
    }
}
