package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.record.HabitRecord;

import java.time.Instant;

public record UpdateHabitRecordResult(
        String id,
        String habitId,
        Instant completedAt,
        Integer minutes,
        String memo
) {
    public static UpdateHabitRecordResult from(HabitRecord record) {
        return new UpdateHabitRecordResult(
                record.getId().value(),
                record.getHabitId().value(),
                record.getCompletedAt(),
                record.hasDuration() ? record.getDuration().minutes() : null,
                record.getMemo() != null ? record.getMemo().value() : null
        );
    }
}
