package com.dochiri.userservice.application.port.in.dto;

import static java.util.Objects.requireNonNull;

public record CreateSocialUserCommand(
        String nickname,
        String profileImageUrl
) {
    public CreateSocialUserCommand {
        requireNonNull(nickname);
        requireNonNull(profileImageUrl);
    }
}