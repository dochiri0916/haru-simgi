package com.dochiri.habitservice.application.port.in.dto;

import java.time.Instant;
import java.util.List;

public record GetHabitRecordsResult(
        String habitId,
        List<RecordDto> records
) {
    public record RecordDto(
            String id,
            Instant completedAt,
            Integer minutes
    ) {
    }
}