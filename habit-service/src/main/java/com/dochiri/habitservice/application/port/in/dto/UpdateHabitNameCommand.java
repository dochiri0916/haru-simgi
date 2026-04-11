package com.dochiri.habitservice.application.port.in.dto;

public record UpdateHabitNameCommand(
        String habitId,
        String ownerReferenceId,
        String newName
) {
}