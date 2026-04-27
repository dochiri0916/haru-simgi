package com.dochiri.authservice.application.port.in.dto;

import com.dochiri.security.role.UserRole;

import static java.util.Objects.requireNonNull;

public record IssueAuthTokenCommand(
        String publicId,
        UserRole role
) {
    public IssueAuthTokenCommand {
        requireNonNull(publicId);
        requireNonNull(role);
    }
}
