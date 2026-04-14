package com.dochiri.habitservice.application.port.in.dto;

public record DeleteHabitCommand(
        String habitId,
        String ownerPublicId
) {
}
