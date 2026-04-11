package com.dochiri.habitservice.application.port.in.dto;

import java.time.Instant;

public record CreateHabitRecordResult(
        String id,
        String habitId,
        Instant completedAt,
        Integer minutes
) {
}