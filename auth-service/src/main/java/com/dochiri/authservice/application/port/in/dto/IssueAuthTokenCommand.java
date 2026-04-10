package com.dochiri.authservice.application.port.in.dto;

import com.dochiri.security.role.UserRole;

import static java.util.Objects.requireNonNull;

public record IssueAuthTokenCommand(
        Long userId,
        UserRole role
) {
    public IssueAuthTokenCommand {
        requireNonNull(userId);
        requireNonNull(role);
    }
}