package com.dochiri.habitservice.infrastructure.adapter.in.web.internal.response;

import com.dochiri.habitservice.application.port.in.dto.MigrateGuestHabitsResult;

public record MigrateGuestHabitsResponse(int migratedCount) {

    public static MigrateGuestHabitsResponse from(MigrateGuestHabitsResult result) {
        return new MigrateGuestHabitsResponse(result.migratedCount());
    }
}
