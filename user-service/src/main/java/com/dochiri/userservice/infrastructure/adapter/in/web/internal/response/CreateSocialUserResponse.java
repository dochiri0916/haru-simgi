package com.dochiri.userservice.infrastructure.adapter.in.web.internal.response;

import com.dochiri.userservice.application.port.in.dto.CreateSocialUserResult;

public record CreateSocialUserResponse(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl
) {
    public static CreateSocialUserResponse from(CreateSocialUserResult result) {
        return new CreateSocialUserResponse(
                result.userId(),
                result.email(),
                result.nickname(),
                result.profileImageUrl()
        );
    }
}
