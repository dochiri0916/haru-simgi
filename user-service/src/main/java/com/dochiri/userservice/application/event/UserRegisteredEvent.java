package com.dochiri.userservice.application.event;

public record UserRegisteredEvent(
        Long userId,
        String publicId,
        String email,
        String passwordHash,
        String role
) {
}
