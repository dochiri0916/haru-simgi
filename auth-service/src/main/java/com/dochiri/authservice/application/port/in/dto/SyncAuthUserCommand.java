package com.dochiri.authservice.application.port.in.dto;

public record SyncAuthUserCommand(
        Long userId,
        String publicId,
        String email,
        String passwordHash,
        String role
) {
}
