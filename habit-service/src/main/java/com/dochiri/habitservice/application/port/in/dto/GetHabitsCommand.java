package com.dochiri.habitservice.application.port.in.dto;

public record GetHabitsCommand(
        String ownerPublicId
) {
}
