package com.dochiri.authservice.application.port.in.dto;

import com.dochiri.security.role.UserRole;

import java.time.Instant;

public record IssueAuthTokenResult(
        String accessToken,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        UserRole role
) {
}