package com.dochiri.userservice.infrastructure.adapter.in.web.internal.response;

import com.dochiri.userservice.application.port.in.dto.CreateUserResult;

public record CreateSocialUserResponse(
        Long userId,
        String nickname,
        String profileImageUrl
) {
    public static CreateSocialUserResponse from(CreateUserResult result) {
        return new CreateSocialUserResponse(
                result.userId(),
                result.nickname(),
                result.profileImageUrl()
        );
    }
}
