package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitResult;

public record CreateHabitResponse(String id, String name) {

    public static CreateHabitResponse from(CreateHabitResult result) {
        return new CreateHabitResponse(result.id(), result.name());
    }

}