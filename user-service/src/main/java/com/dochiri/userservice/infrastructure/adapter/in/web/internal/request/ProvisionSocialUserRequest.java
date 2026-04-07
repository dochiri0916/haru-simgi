package com.dochiri.userservice.infrastructure.adapter.in.web.internal.request;

import com.dochiri.userservice.application.port.in.dto.ProvisionSocialUserCommand;
import jakarta.validation.constraints.Email;
import org.springframework.util.StringUtils;

public record ProvisionSocialUserRequest(
        @Email String email,
        String nickname,
        String profileImageUrl
) {
    public ProvisionSocialUserCommand toCommand() {
        return new ProvisionSocialUserCommand(
                StringUtils.hasText(email) ? email : null,
                StringUtils.hasText(nickname) ? nickname : null,
                StringUtils.hasText(profileImageUrl) ? profileImageUrl : null
        );
    }
}
