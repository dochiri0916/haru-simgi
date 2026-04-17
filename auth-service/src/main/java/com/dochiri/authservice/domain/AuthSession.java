package com.dochiri.authservice.domain;

import com.dochiri.security.role.UserRole;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public record AuthSession(
        String sessionId,
        Long userId,
        String publicId,
        UserRole role,
        Instant issuedAt,
        Instant expiresAt
) {

    public AuthSession {
        requireNonNull(sessionId);
        requireNonNull(userId);
        requireNonNull(publicId);
        requireNonNull(role);
        requireNonNull(issuedAt);
        requireNonNull(expiresAt);
    }

    public static AuthSession create(
            String sessionId,
            Long userId,
            String publicId,
            UserRole role,
            Instant issuedAt,
            Instant expiresAt
    ) {
        return new AuthSession(sessionId, userId, publicId, role, issuedAt, expiresAt);
    }
}
