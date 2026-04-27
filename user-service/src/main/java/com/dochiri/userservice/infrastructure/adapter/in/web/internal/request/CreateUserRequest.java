package com.dochiri.userservice.infrastructure.adapter.in.web.internal.request;

import com.dochiri.userservice.application.port.in.dto.CreateUserCommand;
import com.dochiri.userservice.domain.Nickname;
import com.dochiri.userservice.domain.ProfileImageUrl;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        String idempotencyKey,
        @NotBlank String nickname,
        @NotBlank String profileImageUrl
) {
    public CreateUserCommand toCommand() {
        return new CreateUserCommand(
                idempotencyKey,
                Nickname.of(nickname),
                ProfileImageUrl.of(profileImageUrl)
        );
    }
}
