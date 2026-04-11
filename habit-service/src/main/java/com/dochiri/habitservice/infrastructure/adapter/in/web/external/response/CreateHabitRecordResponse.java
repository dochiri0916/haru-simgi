package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordResult;

import java.time.Instant;

public record CreateHabitRecordResponse(String id, String habitId, Instant completedAt, int value) {

    public static CreateHabitRecordResponse from(CreateHabitRecordResult result) {
        return new CreateHabitRecordResponse(result.id(), result.habitId(), result.completedAt(), result.minutes());
    }

}
