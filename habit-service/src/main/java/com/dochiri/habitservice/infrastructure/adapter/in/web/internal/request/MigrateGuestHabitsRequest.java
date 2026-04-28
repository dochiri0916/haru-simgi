package com.dochiri.habitservice.infrastructure.adapter.in.web.internal.request;

import com.dochiri.habitservice.application.port.in.dto.MigrateGuestHabitsCommand;
import jakarta.validation.constraints.NotBlank;

public record MigrateGuestHabitsRequest(
        @NotBlank String guestId,
        @NotBlank String userPublicId
) {

    public MigrateGuestHabitsCommand toCommand() {
        return new MigrateGuestHabitsCommand(guestId, userPublicId);
    }
}
