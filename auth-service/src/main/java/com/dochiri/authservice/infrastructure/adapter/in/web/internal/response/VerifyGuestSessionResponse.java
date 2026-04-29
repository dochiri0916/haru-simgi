package com.dochiri.authservice.infrastructure.adapter.in.web.internal.response;

import com.dochiri.authservice.application.port.in.dto.VerifyGuestSessionResult;
import com.dochiri.authservice.domain.GuestSessionStatus;

import java.time.Instant;

public record VerifyGuestSessionResponse(
        String guestId,
        GuestSessionStatus status,
        Instant expiresAt
) {

    public static VerifyGuestSessionResponse from(VerifyGuestSessionResult result) {
        return new VerifyGuestSessionResponse(result.guestId(), result.status(), result.expiresAt());
    }
}
