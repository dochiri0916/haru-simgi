package com.dochiri.habitservice.domain.record;

import com.dochiri.habitservice.domain.record.exception.InvalidCompletedAtException;

import java.time.Instant;

public record HabitCompletion(
        Instant completedAt,
        HabitDuration duration,
        HabitMemo memo
) {
    public HabitCompletion {
        if (completedAt == null) {
            throw new InvalidCompletedAtException();
        }
    }

    public static HabitCompletion of(Instant completedAt, Integer minutes, String memo) {
        return new HabitCompletion(
                completedAt,
                minutes == null ? null : HabitDuration.of(minutes),
                HabitMemo.of(memo)
        );
    }
}
