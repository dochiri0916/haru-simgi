package com.dochiri.userservice.infrastructure.adapter.in.web.internal.response;

import com.dochiri.userservice.application.port.in.dto.ProvisionSocialUserResult;

public record ProvisionSocialUserResponse(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl
) {
    public static ProvisionSocialUserResponse from(ProvisionSocialUserResult result) {
        return new ProvisionSocialUserResponse(
                result.userId(),
                result.email(),
                result.nickname(),
                result.profileImageUrl()
        );
    }
}
