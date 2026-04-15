package com.dochiri.habitservice.application.port.in.dto;

import java.time.LocalDate;

public record GetHabitRecordsCommand(
        String habitId,
        String ownerPublicId,
        LocalDate fromDate,
        LocalDate toDate
) {
    public static GetHabitRecordsCommand of(String habitId, String ownerPublicId, LocalDate from, LocalDate to) {
        return new GetHabitRecordsCommand(
                habitId,
                ownerPublicId,
                from,
                to
        );
    }
}
