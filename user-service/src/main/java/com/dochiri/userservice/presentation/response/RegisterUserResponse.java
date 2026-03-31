package com.dochiri.userservice.presentation.response;

import com.dochiri.userservice.application.port.in.dto.RegisterUserResult;

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