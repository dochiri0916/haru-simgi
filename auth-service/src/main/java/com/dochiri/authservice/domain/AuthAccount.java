package com.dochiri.authservice.domain;

import com.dochiri.security.role.UserRole;

import static java.util.Objects.requireNonNull;

public record AuthAccount(
        String publicId,
        AuthProvider provider,
        String providerId,
        UserRole role
) {
    public AuthAccount {
        requireNonNull(provider, "provider는 필수입니다.");
        requireNonNull(role, "role은 필수입니다.");
        requireNonBlank(publicId, "publicId는 비어 있을 수 없습니다.");
        requireNonBlank(providerId, "providerId는 비어 있을 수 없습니다.");
    }

    public AuthAccount changeRole(UserRole role) {
        return new AuthAccount(publicId, provider, providerId, role);
    }

    private static void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
