package com.dochiri.habitservice.application.port.out;

public record HabitGrassAggregation(
        int completedCount,
        int totalMinutes
) {
}
