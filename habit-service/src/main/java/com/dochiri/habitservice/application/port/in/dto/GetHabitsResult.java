package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.HabitType;

import java.util.List;

public record GetHabitsResult(
    List<HabitDto> habits
) {
    public record HabitDto(
        String id,
        String name,
        HabitType type
    ) {
    }
}