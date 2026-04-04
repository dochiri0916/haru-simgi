package com.dochiri.authservice.domain;

public record AuthAccount(
        Long userId,
        String publicId,
        String email,
        String passwordHash,
        String role
) {
}
