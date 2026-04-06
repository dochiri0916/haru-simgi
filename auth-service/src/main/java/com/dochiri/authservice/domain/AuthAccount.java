package com.dochiri.authservice.domain;

import com.dochiri.security.role.UserRole;

public record AuthAccount(
        Long userId,
        String publicId,
        String email,
        String passwordHash,
        UserRole role
) {
}
