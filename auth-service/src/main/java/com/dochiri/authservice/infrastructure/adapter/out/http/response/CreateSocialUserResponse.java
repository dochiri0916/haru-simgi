package com.dochiri.authservice.infrastructure.adapter.out.http.response;

import com.dochiri.authservice.application.port.out.dto.CreateSocialUserResult;

public record CreateSocialUserResponse(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl
) {
    public CreateSocialUserResult toResult() {
        return new CreateSocialUserResult(userId, email, nickname, profileImageUrl);
    }
}
