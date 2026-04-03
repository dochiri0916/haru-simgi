package com.dochiri.userservice.infrastructure.adapter.in.web.publicapi.response;

import com.dochiri.userservice.application.command.port.in.dto.RegisterUserResult;

public record RegisterUserResponse(
        String publicId,
        String email
) {
    public static RegisterUserResponse from(RegisterUserResult result) {
        return new RegisterUserResponse(
                result.publicId(),
                result.email()
        );
    }
}
