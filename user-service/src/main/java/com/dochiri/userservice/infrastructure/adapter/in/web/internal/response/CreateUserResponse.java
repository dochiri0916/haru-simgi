package com.dochiri.userservice.infrastructure.adapter.in.web.internal.response;

import com.dochiri.userservice.application.port.in.dto.CreateUserResult;

public record CreateUserResponse(
        String publicId,
        String nickname,
        String profileImageUrl
) {
    public static CreateUserResponse from(CreateUserResult result) {
        return new CreateUserResponse(
                result.publicId(),
                result.nickname(),
                result.profileImageUrl()
        );
    }
}
