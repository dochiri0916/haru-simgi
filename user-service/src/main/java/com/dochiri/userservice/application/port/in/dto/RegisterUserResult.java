package com.dochiri.userservice.application.port.in.dto;

import com.dochiri.security.role.UserRole;

public record RegisterUserResult(
        String publicId,
        String email,
        UserRole role
) {
}
