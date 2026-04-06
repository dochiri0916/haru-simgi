package com.dochiri.authservice.infrastructure.adapter.in.messaging.kafka;

import com.dochiri.authservice.application.port.in.dto.SyncAuthUserCommand;

public record UserRegisteredMessage(
        Long userId,
        String publicId,
        String email,
        String passwordHash,
        String role
) {

    public SyncAuthUserCommand toCommand() {
        return new SyncAuthUserCommand(userId, publicId, email, passwordHash, role);
    }
}
