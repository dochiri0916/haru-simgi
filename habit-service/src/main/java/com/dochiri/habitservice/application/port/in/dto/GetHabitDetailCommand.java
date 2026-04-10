package com.dochiri.habitservice.application.port.in.dto;

public record GetHabitDetailCommand(
    String habitId,
    String ownerReferenceId
) {
}
