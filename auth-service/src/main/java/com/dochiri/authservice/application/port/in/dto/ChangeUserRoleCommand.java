package com.dochiri.authservice.application.port.in.dto;

import com.dochiri.security.role.UserRole;

import static java.util.Objects.requireNonNull;

public record ChangeUserRoleCommand(
        Long userId,
        UserRole role
) {
    public ChangeUserRoleCommand {
        requireNonNull(userId);
        requireNonNull(role);
    }
}