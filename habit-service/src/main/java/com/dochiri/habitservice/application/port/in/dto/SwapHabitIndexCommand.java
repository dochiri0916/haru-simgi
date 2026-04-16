package com.dochiri.habitservice.application.port.in.dto;

public record SwapHabitIndexCommand(
        String sourceHabitId,
        String targetHabitId,
        String ownerPublicId
) {
}
