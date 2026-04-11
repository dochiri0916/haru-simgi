package com.dochiri.habitservice.application.port.in.dto;

import java.time.Instant;

public record GetHabitRecordsCommand(
        String habitId,
        String ownerReferenceId,
        Instant fromDate,
        Instant toDate
) {
}