package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.Habit;

public record UpdateHabitNameResult(
        HabitView habit
) {
    public static UpdateHabitNameResult from(Habit habit) {
        return new UpdateHabitNameResult(HabitView.from(habit));
    }
}
