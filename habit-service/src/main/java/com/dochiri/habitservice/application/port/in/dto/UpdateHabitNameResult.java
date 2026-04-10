package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.HabitType;

public record UpdateHabitNameResult(
    String id,
    String name,
    HabitType type
) {
}
