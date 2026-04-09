package com.dochiri.authservice.infrastructure.adapter.out.http.request;

public record CreateSocialUserRequest(
        String nickname,
        String profileImageUrl
) {
}
