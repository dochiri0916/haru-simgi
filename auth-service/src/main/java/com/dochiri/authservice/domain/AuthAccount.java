package com.dochiri.authservice.domain;

import com.dochiri.security.role.UserRole;

public record AuthAccount(
        Long userId,
        String publicId,
        AuthProvider provider,
        String providerUserId,
        String passwordHash,
        UserRole role
) {
}