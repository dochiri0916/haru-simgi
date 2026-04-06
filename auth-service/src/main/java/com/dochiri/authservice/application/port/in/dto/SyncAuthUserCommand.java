package com.dochiri.authservice.application.port.in.dto;

import com.dochiri.security.role.UserRole;

public record SyncAuthUserCommand(
        Long userId,
        String publicId,
        String email,
        String passwordHash,
        UserRole role
) {
}
