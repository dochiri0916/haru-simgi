package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.application.common.Patchable;
import com.dochiri.habitservice.domain.habit.HabitOwner;

import java.time.Instant;

public record UpdateHabitRecordCommand(
        String habitId,
        String recordId,
        HabitOwner owner,
        Instant completedAt,
        Integer minutes,
        Patchable<String> memo
) {
    public UpdateHabitRecordCommand {
        if (memo == null) {
            memo = Patchable.undefined();
        }
    }
}
