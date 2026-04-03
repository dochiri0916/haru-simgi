package com.dochiri.userservice.infrastructure.adapter.out.messaging.kafka;

import com.dochiri.userservice.application.event.UserRegisteredEvent;

public record UserRegisteredMessage(
        Long userId,
        String publicId,
        String email,
        String password,
        String role
) {

    public static UserRegisteredMessage from(UserRegisteredEvent event) {
        return new UserRegisteredMessage(
                event.userId(),
                event.publicId(),
                event.email(),
                event.password(),
                event.role()
        );
    }

}
