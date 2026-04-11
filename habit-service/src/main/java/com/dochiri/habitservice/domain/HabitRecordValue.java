package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.exception.InvalidHabitRecordValueException;

public record HabitRecordValue(
        int value
) {
    public HabitRecordValue {
        if (value < 0) {
            throw new InvalidHabitRecordValueException(value);
        }
    }

    public static HabitRecordValue of(int value) {
        return new HabitRecordValue(value);
    }

}