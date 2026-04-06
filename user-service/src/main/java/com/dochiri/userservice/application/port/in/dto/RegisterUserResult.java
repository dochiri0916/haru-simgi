package com.dochiri.userservice.application.port.in.dto;

import com.dochiri.security.role.UserRole;
import com.dochiri.userservice.domain.User;

public record RegisterUserResult(
        String publicId,
        String email,
        UserRole role
) {
    public static RegisterUserResult from(User user, UserRole role) {
        return new RegisterUserResult(
                user.getPublicId(),
                user.getEmail(),
                role
        );
    }
}
