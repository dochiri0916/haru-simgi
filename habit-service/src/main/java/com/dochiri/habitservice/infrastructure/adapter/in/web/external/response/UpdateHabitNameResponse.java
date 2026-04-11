package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameResult;

public record UpdateHabitNameResponse(String id, String name) {

    public static UpdateHabitNameResponse from(UpdateHabitNameResult result) {
        return new UpdateHabitNameResponse(result.id(), result.name());
    }

}
