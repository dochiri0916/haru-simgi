package com.dochiri.authservice.presentation.request;

import com.dochiri.authservice.application.port.in.dto.SyncAuthUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SyncAuthUserRequest(
        @NotNull
        Long userId,

        @NotBlank
        String publicId,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String passwordHash,

        @NotBlank
        String role
) {
    public SyncAuthUserCommand toCommand() {
        return new SyncAuthUserCommand(userId, publicId, email, passwordHash, role);
    }
}
