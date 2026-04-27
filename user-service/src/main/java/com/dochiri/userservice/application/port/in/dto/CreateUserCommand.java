package com.dochiri.userservice.application.port.in.dto;

import com.dochiri.userservice.domain.Nickname;
import com.dochiri.userservice.domain.ProfileImageUrl;

import static java.util.Objects.requireNonNull;

public record CreateUserCommand(
        String idempotencyKey,
        Nickname nickname,
        ProfileImageUrl profileImageUrl
) {
    public CreateUserCommand {
        requireNonNull(nickname);
        requireNonNull(profileImageUrl);
    }
}
