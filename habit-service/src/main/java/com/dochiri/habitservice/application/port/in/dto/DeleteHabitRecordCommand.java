package com.dochiri.habitservice.application.port.in.dto;

public record DeleteHabitRecordCommand(
        String habitId,
        String recordId,
        String ownerPublicId
) {
}
