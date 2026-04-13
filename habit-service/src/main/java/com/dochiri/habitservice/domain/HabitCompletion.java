package com.dochiri.habitservice.domain;

import java.time.Instant;

public record HabitCompletion(
        Instant completedAt,
        HabitDuration duration
) {
    public HabitCompletion {
        if (completedAt == null) {
            throw new IllegalArgumentException("completedAt must not be null");
        }
    }

    public static HabitCompletion withoutDuration(Instant completedAt) {
        return new HabitCompletion(completedAt, null);
    }

    public static HabitCompletion withDuration(Instant completedAt, HabitDuration duration) {
        return new HabitCompletion(completedAt, duration);
    }

    public boolean hasDuration() {
        return duration != null;
    }
}
