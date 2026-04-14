package com.dochiri.habitservice.application.port.in.dto;

public record UpdateHabitNameCommand(
        String habitId,
        String ownerPublicId,
        String newName
) {
}
