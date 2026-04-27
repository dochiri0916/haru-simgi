package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.HabitOwner;

public record DeleteHabitRecordCommand(
        String habitId,
        String recordId,
        HabitOwner owner
) {
}
