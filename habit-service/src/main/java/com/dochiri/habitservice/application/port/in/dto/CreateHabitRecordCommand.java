package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.HabitOwner;

import java.time.Instant;

public record CreateHabitRecordCommand(
        String habitId,
        HabitOwner owner,
        Instant completedAt,
        Integer minutes,
        String memo
) {
}
