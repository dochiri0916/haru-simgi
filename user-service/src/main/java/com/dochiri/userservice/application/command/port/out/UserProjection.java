package com.dochiri.userservice.application.command.port.out;

public record UserProjection(
        Long userId,
        String publicId,
        String email,
        String role
) {
}
