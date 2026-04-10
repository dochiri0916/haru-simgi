package com.dochiri.authservice.application.port.out.dto;

import static java.util.Objects.requireNonNull;

public record CreateSocialUserResult(
        Long userId,
        String nickname,
        String profileImageUrl
) {
    public CreateSocialUserResult {
        requireNonNull(userId);
    }
}