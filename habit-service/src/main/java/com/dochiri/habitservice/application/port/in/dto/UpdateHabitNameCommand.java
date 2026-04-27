package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.HabitOwner;

public record UpdateHabitNameCommand(
        String habitId,
        HabitOwner owner,
        String newName
) {
}
