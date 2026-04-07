package com.dochiri.authservice.application.port.out.dto;

import static java.util.Objects.requireNonNull;

public record ProvisionedSocialUser(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl
) {
    public ProvisionedSocialUser {
        requireNonNull(userId);
    }
}
