package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.Habit;

import java.util.List;

public record SwapHabitIndexResult(
        List<HabitView> habits
) {
    public static SwapHabitIndexResult from(List<Habit> habits) {
        return new SwapHabitIndexResult(habits.stream().map(HabitView::from).toList());
    }
}
