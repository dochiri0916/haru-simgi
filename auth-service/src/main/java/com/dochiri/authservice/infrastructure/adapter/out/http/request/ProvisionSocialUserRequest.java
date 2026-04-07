package com.dochiri.authservice.infrastructure.adapter.out.http.request;

public record ProvisionSocialUserRequest(
        String email,
        String nickname,
        String profileImageUrl
) {
}
