package com.dochiri.authservice.domain;

public record AuthUser(
        Long userId,
        String publicId,
        String email,
        String passwordHash,
        String role
) {
}