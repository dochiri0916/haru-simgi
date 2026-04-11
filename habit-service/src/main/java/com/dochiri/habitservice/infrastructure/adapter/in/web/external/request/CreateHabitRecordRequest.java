package com.dochiri.habitservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordCommand;

import java.time.Instant;

public record CreateHabitRecordRequest(Instant completedAt, int value) {

    public CreateHabitRecordCommand toCommand(String habitId, String userId) {
        return new CreateHabitRecordCommand(habitId, userId, completedAt, value);
    }

}
