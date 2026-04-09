package com.dochiri.authservice.application.port.out.dto;

import static java.util.Objects.requireNonNull;

public record KakaoUserProfileResult(
        Long id,
        String email,
        String nickname,
        String profileImageUrl
) {
    public KakaoUserProfileResult {
        requireNonNull(id);
    }
}
