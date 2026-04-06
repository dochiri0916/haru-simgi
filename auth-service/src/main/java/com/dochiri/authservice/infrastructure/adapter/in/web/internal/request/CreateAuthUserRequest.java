package com.dochiri.authservice.infrastructure.adapter.in.web.internal.request;

import com.dochiri.authservice.application.port.in.dto.SyncAuthUserCommand;
import com.dochiri.security.role.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAuthUserRequest(
        @NotNull Long userId,
        @NotBlank String publicId,
        @Email @NotBlank String email,
        @NotBlank String passwordHash,
        @NotNull UserRole role
) {
    public SyncAuthUserCommand toCommand() {
        return new SyncAuthUserCommand(userId, publicId, email, passwordHash, role);
    }
}
