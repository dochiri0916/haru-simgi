package com.dochiri.authservice.application.port.in.dto;

import com.dochiri.authservice.domain.GuestSessionStatus;

import java.time.Instant;

public record GetGuestSessionResult(
        String guestId,
        GuestSessionStatus status,
        Instant expiresAt
) {
}
