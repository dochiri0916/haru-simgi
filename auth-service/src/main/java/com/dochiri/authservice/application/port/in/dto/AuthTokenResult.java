package com.dochiri.authservice.application.port.in.dto;

import com.dochiri.security.jwt.JwtTokenResult;
import com.dochiri.security.role.UserRole;

import java.time.Instant;

public record AuthTokenResult(
        String accessToken,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        UserRole role
) {
    public static AuthTokenResult from(JwtTokenResult result, UserRole role) {
        return new AuthTokenResult(
                result.accessToken(),
                result.refreshToken(),
                result.refreshTokenExpiresAt(),
                role
        );
    }
}
