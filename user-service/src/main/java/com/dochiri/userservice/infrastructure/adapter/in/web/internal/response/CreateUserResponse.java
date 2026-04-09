package com.dochiri.userservice.infrastructure.adapter.in.web.internal.response;

import com.dochiri.userservice.application.port.in.dto.CreateUserResult;

public record CreateUserResponse(
        Long userId,
        String email
) {
    public static CreateUserResponse from(CreateUserResult result) {
        return new CreateUserResponse(result.userId(), result.email());
    }
}
