package com.dochiri.userservice.application.event;

import com.dochiri.userservice.domain.User;

public record UserRegisteredEvent(
        Long userId,
        String publicId,
        String email,
        String password,
        String role
) {
    public static UserRegisteredEvent of(User user, String password) {
        return new UserRegisteredEvent(
                user.getUserId(),
                user.getId().value(),
                user.getEmail(),
                password,
                "USER"
        );
    }
}