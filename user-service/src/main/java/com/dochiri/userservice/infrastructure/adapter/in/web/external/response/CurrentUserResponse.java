package com.dochiri.userservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.security.role.UserRole;
import com.dochiri.userservice.application.port.in.dto.GetCurrentUserResult;

public record CurrentUserResponse(
        Long userId,
        String id,
        String nickname,
        String profileImageUrl,
        UserRole role
) {

    public static CurrentUserResponse from(Long userId, GetCurrentUserResult result, UserRole role) {
        return new CurrentUserResponse(
                userId,
                result.id(),
                result.nickname(),
                result.profileImageUrl(),
                role
        );
    }
}