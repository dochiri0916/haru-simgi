package com.dochiri.habitservice.application.port.in.dto;

import java.time.LocalDate;

public record GetHabitGrassCommand(
        String ownerReferenceId,
        LocalDate fromDate,
        LocalDate toDate
) {
}