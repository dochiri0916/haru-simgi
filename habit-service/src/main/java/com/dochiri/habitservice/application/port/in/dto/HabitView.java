package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.Habit;

import java.time.Instant;

public record HabitView(
        String id,
        String name,
        String color,
        String colorHex,
        int index,
        Instant createdAt
) {
    public static HabitView from(Habit habit) {
        return new HabitView(
                habit.getId().value(),
                habit.getName().value(),
                habit.getColor().colorType().name(),
                habit.getColor().getHexValue(),
                habit.getIndex().value(),
                habit.getCreatedAt()
        );
    }
}
