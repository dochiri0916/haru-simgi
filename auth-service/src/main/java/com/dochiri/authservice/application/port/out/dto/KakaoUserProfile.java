package com.dochiri.authservice.application.port.out.dto;

import static java.util.Objects.requireNonNull;

public record KakaoUserProfile(
        Long id,
        String email,
        String nickname,
        String profileImageUrl
) {
    public KakaoUserProfile {
        requireNonNull(id);
    }
}
