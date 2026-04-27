package com.dochiri.authservice.application.port.out.dto;

import static java.util.Objects.requireNonNull;

public record CreateSocialUserResult(
        String publicId,
        String nickname,
        String profileImageUrl
) {
    public CreateSocialUserResult {
        requireNonNull(publicId);
    }
}
