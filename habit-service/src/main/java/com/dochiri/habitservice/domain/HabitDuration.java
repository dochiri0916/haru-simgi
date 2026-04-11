package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.exception.InvalidHabitDurationException;

public record HabitDuration(int minutes) {

    public HabitDuration {
        if (minutes < 0) {
            throw new InvalidHabitDurationException(minutes);
        }

        if (minutes > 24 * 60) {
            throw new InvalidHabitDurationException(minutes);
        }
    }

    public static HabitDuration of(int value) {
        return new HabitDuration(value);
    }

}