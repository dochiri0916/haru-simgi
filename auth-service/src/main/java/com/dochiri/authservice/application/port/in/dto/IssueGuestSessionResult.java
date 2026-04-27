package com.dochiri.authservice.application.port.in.dto;

import java.time.Instant;

public record IssueGuestSessionResult(
        String guestId,
        String token,
        Instant expiresAt
) {
}
