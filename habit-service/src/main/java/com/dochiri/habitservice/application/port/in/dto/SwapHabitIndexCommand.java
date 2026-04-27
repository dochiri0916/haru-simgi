package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.HabitOwner;

public record SwapHabitIndexCommand(
        String sourceHabitId,
        String targetHabitId,
        HabitOwner owner
) {
}
