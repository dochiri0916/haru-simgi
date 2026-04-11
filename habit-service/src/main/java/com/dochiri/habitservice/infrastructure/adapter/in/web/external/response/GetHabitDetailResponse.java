package com.dochiri.habitservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailResult;

public record GetHabitDetailResponse(String id, String name) {

    public static GetHabitDetailResponse from(GetHabitDetailResult result) {
        return new GetHabitDetailResponse(result.id(), result.name());
    }

}
