package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.Habit;

public record CreateHabitResult(
        HabitView habit
) {
    public static CreateHabitResult from(Habit habit) {
        return new CreateHabitResult(HabitView.from(habit));
    }
}
