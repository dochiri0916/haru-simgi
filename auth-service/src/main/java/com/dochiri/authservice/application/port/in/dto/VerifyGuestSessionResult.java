package com.dochiri.authservice.application.port.in.dto;

import com.dochiri.authservice.domain.GuestSessionStatus;

import java.time.Instant;

public record VerifyGuestSessionResult(
        String guestId,
        GuestSessionStatus status,
        Instant expiresAt
) {
}
