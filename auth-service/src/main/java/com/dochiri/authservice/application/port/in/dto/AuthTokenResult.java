package com.dochiri.authservice.application.port.in.dto;

import com.dochiri.security.jwt.JwtTokenResult;

import java.time.Instant;

public record AuthTokenResult(
        String accessToken,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
    public static AuthTokenResult from(JwtTokenResult result) {
        return new AuthTokenResult(
                result.accessToken(),
                result.refreshToken(),
                result.refreshTokenExpiresAt()
        );
    }
}
