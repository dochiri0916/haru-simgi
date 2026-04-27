package com.dochiri.authservice.infrastructure.adapter.out.http.request;

public record CreateSocialUserRequest(
        String idempotencyKey,
        String nickname,
        String profileImageUrl
) {
}
