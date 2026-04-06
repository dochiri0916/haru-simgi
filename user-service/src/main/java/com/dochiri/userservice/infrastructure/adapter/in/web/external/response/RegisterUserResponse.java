package com.dochiri.userservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.security.role.UserRole;
import com.dochiri.userservice.application.port.in.dto.RegisterUserResult;

public record RegisterUserResponse(
        String publicId,
        String email,
        UserRole role
) {
    public static RegisterUserResponse from(RegisterUserResult result) {
        return new RegisterUserResponse(
                result.publicId(),
                result.email(),
                result.role()
        );
    }
}
