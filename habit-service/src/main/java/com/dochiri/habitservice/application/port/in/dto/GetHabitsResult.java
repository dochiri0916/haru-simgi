package com.dochiri.habitservice.application.port.in.dto;

import java.util.List;

public record GetHabitsResult(
        List<HabitDto> habits
) {
    public record HabitDto(
            String id,
            String name
    ) {
    }
}