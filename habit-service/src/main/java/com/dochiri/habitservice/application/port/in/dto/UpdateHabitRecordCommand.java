package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.HabitOwner;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.Instant;

public record UpdateHabitRecordCommand(
        String habitId,
        String recordId,
        HabitOwner owner,
        Instant completedAt,
        Integer minutes,
        JsonNullable<String> memo
) {
    public UpdateHabitRecordCommand {
        if (memo == null) {
            memo = JsonNullable.undefined();
        }
    }
}
