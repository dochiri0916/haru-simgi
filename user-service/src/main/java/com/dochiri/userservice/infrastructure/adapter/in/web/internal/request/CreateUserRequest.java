package com.dochiri.userservice.infrastructure.adapter.in.web.internal.request;

import com.dochiri.userservice.application.port.in.dto.CreateUserCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String nickname,
        @NotBlank String profileImageUrl
) {
    public CreateUserCommand toCommand() {
        return new CreateUserCommand(
                nickname,
                profileImageUrl
        );
    }
}
