package com.dochiri.userservice.application.port.out;

public record UserProjection(
        Long userId,
        String publicId,
        String email,
        String role
) {
}
