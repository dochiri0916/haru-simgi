package com.dochiri.security.jwt;

public record JwtPrincipal(
        String publicId,
        String role
) {
}