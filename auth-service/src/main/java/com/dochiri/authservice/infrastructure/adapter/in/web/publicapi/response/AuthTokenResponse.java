package com.dochiri.authservice.infrastructure.adapter.in.web.publicapi.response;

import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;

import java.time.Instant;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
    public static AuthTokenResponse from(AuthTokenResult result) {
        return new AuthTokenResponse(
                result.accessToken(),
                result.refreshToken(),
                result.refreshTokenExpiresAt()
        );
    }
}
