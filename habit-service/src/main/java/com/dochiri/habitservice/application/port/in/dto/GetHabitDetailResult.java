package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.Habit;

public record GetHabitDetailResult(
        HabitView habit
) {
    public static GetHabitDetailResult from(Habit habit) {
        return new GetHabitDetailResult(HabitView.from(habit));
    }
}
