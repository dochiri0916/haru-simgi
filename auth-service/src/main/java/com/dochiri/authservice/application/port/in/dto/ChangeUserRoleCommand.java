package com.dochiri.authservice.application.port.in.dto;

import com.dochiri.security.role.UserRole;

public record ChangeUserRoleCommand(
        Long userId,
        UserRole role
) {
}
