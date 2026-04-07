package com.dochiri.authservice.infrastructure.adapter.out.http.response;

import com.dochiri.authservice.application.port.out.dto.ProvisionedSocialUser;

public record ProvisionSocialUserResponse(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl
) {
    public ProvisionedSocialUser toResult() {
        return new ProvisionedSocialUser(userId, email, nickname, profileImageUrl);
    }
}
