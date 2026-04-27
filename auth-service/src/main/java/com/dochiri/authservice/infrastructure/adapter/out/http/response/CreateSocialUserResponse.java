package com.dochiri.authservice.infrastructure.adapter.out.http.response;

import com.dochiri.authservice.application.port.out.dto.CreateSocialUserResult;

public record CreateSocialUserResponse(
        String publicId,
        String nickname,
        String profileImageUrl
) {
    public CreateSocialUserResult toResult() {
        return new CreateSocialUserResult(publicId, nickname, profileImageUrl);
    }
}
