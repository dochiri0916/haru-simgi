package com.dochiri.userservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.security.role.UserRole;
import com.dochiri.userservice.domain.User;

public record CurrentUserResponse(
        Long userId,
        String publicId,
        String nickname,
        String profileImageUrl,
        UserRole role
) {

    public static CurrentUserResponse from(Long userId, User user, UserRole role) {
        return new CurrentUserResponse(
                userId,
                user.getPublicId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                role
        );
    }
}
