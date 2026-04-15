package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.Habit;

import java.time.Instant;

public record CreateHabitResult(
        String id,
        String name,
        String color,
        String colorHex,
        int index,
        Instant createdAt
) {
    public static CreateHabitResult from(Habit habit) {
        return new CreateHabitResult(
                habit.getId().value(),
                habit.getName().value(),
                habit.getColor().colorType().name(),
                habit.getColor().getHexValue(),
                habit.getIndex().value(),
                habit.getCreatedAt()
        );
    }
}
