package com.dochiri.userservice.application.event;

import com.dochiri.userservice.domain.User;

public record UserRegisteredEvent(
        Long userId,
        String publicId,
        String email,
        String passwordHash,
        String role
) {
    public static UserRegisteredEvent of(User user, String passwordHash) {
        return new UserRegisteredEvent(
                null,
                user.getId(),
                user.getEmail(),
                passwordHash,
                "USER"
        );
    }
}
