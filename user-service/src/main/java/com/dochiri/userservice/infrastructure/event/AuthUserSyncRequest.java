package com.dochiri.userservice.infrastructure.event;

import com.dochiri.userservice.application.event.UserRegisteredEvent;

public record AuthUserSyncRequest(
        Long userId,
        String publicId,
        String email,
        String passwordHash,
        String role
) {
    public static AuthUserSyncRequest from(UserRegisteredEvent event) {
        return new AuthUserSyncRequest(
                event.userId(),
                event.publicId(),
                event.email(),
                event.passwordHash(),
                event.role()
        );
    }
}
