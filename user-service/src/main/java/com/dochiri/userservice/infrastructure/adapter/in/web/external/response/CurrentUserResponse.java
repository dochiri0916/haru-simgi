package com.dochiri.userservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.userservice.application.port.in.dto.GetCurrentUserResult;

public record CurrentUserResponse(
        String id,
        String nickname,
        String profileImageUrl
) {
    public static CurrentUserResponse from(GetCurrentUserResult result) {
        return new CurrentUserResponse(
                result.id(),
                result.nickname(),
                result.profileImageUrl()
        );
    }
}