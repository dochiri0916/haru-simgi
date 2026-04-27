package com.dochiri.habitservice.application.port.in.dto;

import com.dochiri.habitservice.domain.habit.HabitOwner;

import java.time.LocalDate;

public record GetHabitGrassCommand(
        HabitOwner owner,
        LocalDate fromDate,
        LocalDate toDate
) {
}
