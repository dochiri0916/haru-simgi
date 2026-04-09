package com.dochiri.authservice.infrastructure.adapter.out.http.response;

import com.dochiri.authservice.application.port.out.dto.CreateUserResult;

public record CreateUserResponse(
        Long userId,
        String email
) {
    public CreateUserResult toResult() {
        return new CreateUserResult(userId, email);
    }
}
