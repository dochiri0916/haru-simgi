package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.HabitOwner;

import java.time.LocalDate;

public record GetHabitRecordsCommand(
        String habitId,
        HabitOwner owner,
        LocalDate fromDate,
        LocalDate toDate
) {
    public static GetHabitRecordsCommand of(String habitId, HabitOwner owner, LocalDate from, LocalDate to) {
        return new GetHabitRecordsCommand(
                habitId,
                owner,
                from,
                to
        );
    }
}
