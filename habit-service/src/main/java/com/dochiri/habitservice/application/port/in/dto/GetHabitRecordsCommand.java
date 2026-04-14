package com.dochiri.habitservice.application.port.in.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public record GetHabitRecordsCommand(
        String habitId,
        String ownerPublicId,
        Instant fromDate,
        Instant toDate
) {
    public static GetHabitRecordsCommand of(String habitId, String ownerPublicId, LocalDate from, LocalDate to) {
        return new GetHabitRecordsCommand(
                habitId,
                ownerPublicId,
                from.atStartOfDay(ZoneOffset.UTC).toInstant(),
                to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        );
    }
}
