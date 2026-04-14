package com.dochiri.habitservice.application.port.in.dto;

import java.time.Instant;

public record CreateHabitResult(
        String id,
        String name,
        String color,
        String colorHex,
        int index,
        Instant createdAt
) {
}
