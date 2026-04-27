package com.dochiri.authservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.authservice.application.port.in.dto.IssueGuestSessionResult;
import com.dochiri.authservice.application.port.in.dto.GetGuestSessionResult;
import com.dochiri.authservice.domain.GuestSessionStatus;

import java.time.Instant;

public record GuestSessionResponse(
        String guestId,
        GuestSessionStatus status,
        Instant expiresAt
) {

    public static GuestSessionResponse from(IssueGuestSessionResult result) {
        return new GuestSessionResponse(result.guestId(), GuestSessionStatus.ACTIVE, result.expiresAt());
    }

    public static GuestSessionResponse from(GetGuestSessionResult result) {
        return new GuestSessionResponse(result.guestId(), result.status(), result.expiresAt());
    }
}
