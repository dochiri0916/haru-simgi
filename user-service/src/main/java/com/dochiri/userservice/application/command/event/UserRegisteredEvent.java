package com.dochiri.userservice.application.command.event;

public record UserRegisteredEvent(
        Long userId,
        String publicId,
        String email,
        String password,
        String role
) {
}
