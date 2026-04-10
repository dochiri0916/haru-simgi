package com.dochiri.habitservice.application.port.in.dto;

import java.time.LocalDate;

public record LogHabitResult(
    String logId,
    String habitId,
    LocalDate loggedDate,
    int value
) {
}
