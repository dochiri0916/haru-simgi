package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.Habit;

import java.util.List;

public record GetHabitsResult(
        List<HabitView> habits
) {
    public static GetHabitsResult from(List<Habit> habits) {
        return new GetHabitsResult(habits.stream().map(HabitView::from).toList());
    }
}
