package com.dochiri.userservice.infrastructure.adapter.in.web.internal.request;

import com.dochiri.userservice.application.port.in.dto.CreateSocialUserCommand;
import org.springframework.util.StringUtils;

public record CreateSocialUserRequest(
        String nickname,
        String profileImageUrl
) {
    public CreateSocialUserCommand toCommand() {
        return new CreateSocialUserCommand(
                StringUtils.hasText(nickname) ? nickname : null,
                StringUtils.hasText(profileImageUrl) ? profileImageUrl : null
        );
    }
}
