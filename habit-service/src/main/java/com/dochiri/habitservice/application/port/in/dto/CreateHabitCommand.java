package com.dochiri.habitservice.application.port.in.dto;

public record CreateHabitCommand(
        String ownerPublicId,
        String name,
        String color
) {
}