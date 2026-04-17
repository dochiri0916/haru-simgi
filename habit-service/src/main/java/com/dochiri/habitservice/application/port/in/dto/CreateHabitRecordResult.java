package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.grass.GrassLevelPolicy;
import com.dochiri.habitservice.domain.record.HabitRecord;

import java.time.Instant;

public record CreateHabitRecordResult(
        String id,
        String habitId,
        Instant completedAt,
        int minutes,
        int level,
        String memo
) {
    public static CreateHabitRecordResult from(HabitRecord record) {
        int minutes = record.hasDuration() ? record.getDuration().minutes() : 0;

        return new CreateHabitRecordResult(
                record.getId().value(),
                record.getHabitId().value(),
                record.getCompletedAt(),
                minutes,
                calculateLevel(minutes),
                record.getMemo() != null ? record.getMemo().value() : null
        );
    }

    private static int calculateLevel(int minutes) {
        return Math.max(1, GrassLevelPolicy.calculate(minutes).getLevel());
    }
}
