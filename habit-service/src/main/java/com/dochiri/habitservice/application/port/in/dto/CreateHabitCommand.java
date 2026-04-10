package com.dochiri.habitservice.application.port.in.dto;

public record CreateHabitCommand(
    String ownerReferenceId,
    String name,
    String habitType
) {
}
