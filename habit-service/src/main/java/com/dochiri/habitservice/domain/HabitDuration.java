package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.exception.InvalidHabitDurationException;

public record HabitDuration(
        int minutes
) {
    public HabitDuration {
        if (minutes < 0 || minutes > 24 * 60) {
            throw new InvalidHabitDurationException(minutes);
        }
    }

    public static HabitDuration of(int minutes) {
        return new HabitDuration(minutes);
    }
}