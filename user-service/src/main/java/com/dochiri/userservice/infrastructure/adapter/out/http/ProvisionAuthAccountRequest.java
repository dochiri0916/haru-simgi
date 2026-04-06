package com.dochiri.userservice.infrastructure.adapter.out.http;

import com.dochiri.security.role.UserRole;

public record ProvisionAuthAccountRequest(
        Long userId,
        String publicId,
        String email,
        String passwordHash,
        UserRole role
) {
}
