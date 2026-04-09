package com.dochiri.authservice.domain;

import com.dochiri.security.role.UserRole;

public record AuthAccount(
        Long userId,
        AuthProvider provider,
        String providerUserId,
        String email,
        String passwordHash,
        UserRole role
) {
}