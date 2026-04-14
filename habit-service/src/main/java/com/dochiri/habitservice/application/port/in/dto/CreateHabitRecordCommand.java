package com.dochiri.habitservice.application.port.in.dto;

import java.time.Instant;

public record CreateHabitRecordCommand(
        String habitId,
        String ownerPublicId,
        Instant completedAt,
        Integer minutes
) {
}
