package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.HabitType;

public record CreateHabitResult(
    String id,
    String name,
    HabitType type
) {
}