package com.dochiri.authservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;

import java.time.Instant;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
    public static AuthTokenResponse from(AuthTokenResult result) {
        return new AuthTokenResponse(
                "Bearer",
                result.accessToken(),
                result.refreshToken(),
                result.refreshTokenExpiresAt()
        );
    }
}
