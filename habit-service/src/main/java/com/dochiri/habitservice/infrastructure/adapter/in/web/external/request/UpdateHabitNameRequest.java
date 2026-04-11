package com.dochiri.habitservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameCommand;

public record UpdateHabitNameRequest(String name) {

    public UpdateHabitNameCommand toCommand(String habitId, String userId) {
        return new UpdateHabitNameCommand(habitId, userId, name);
    }

}
