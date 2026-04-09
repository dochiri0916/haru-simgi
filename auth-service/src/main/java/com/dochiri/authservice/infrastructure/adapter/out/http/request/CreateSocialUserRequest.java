package com.dochiri.authservice.infrastructure.adapter.out.http.request;

public record CreateSocialUserRequest(
        String email,
        String nickname,
        String profileImageUrl
) {
}
