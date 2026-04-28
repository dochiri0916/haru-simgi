package com.dochiri.habitservice.infrastructure.adapter.out.http;

import java.time.Instant;

public record GuestSessionResult(
        String guestId,
        String status,
        Instant expiresAt
) {

    public boolean active() {
        return "ACTIVE".equals(status);
    }
}
