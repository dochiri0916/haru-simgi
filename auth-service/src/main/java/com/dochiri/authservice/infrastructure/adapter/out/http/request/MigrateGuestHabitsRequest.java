package com.dochiri.authservice.infrastructure.adapter.out.http.request;

public record MigrateGuestHabitsRequest(
        String guestId,
        String userPublicId
) {
}
