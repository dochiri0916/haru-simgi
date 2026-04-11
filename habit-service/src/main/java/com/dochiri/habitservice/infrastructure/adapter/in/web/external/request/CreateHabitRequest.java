package com.dochiri.habitservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitCommand;

public record CreateHabitRequest(String name) {

    public CreateHabitCommand toCommand(String userId) {
        return new CreateHabitCommand(userId, name);
    }

}