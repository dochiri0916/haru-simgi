package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.HabitOwner;

public record GetHabitDetailCommand(
        String habitId,
        HabitOwner owner
) {
}
