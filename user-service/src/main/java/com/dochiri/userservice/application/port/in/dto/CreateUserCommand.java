package com.dochiri.userservice.application.port.in.dto;

import static java.util.Objects.requireNonNull;

public record CreateUserCommand(
        String nickname,
        String profileImageUrl
) {
    public CreateUserCommand {
        requireNonNull(nickname);
        requireNonNull(profileImageUrl);
    }
}