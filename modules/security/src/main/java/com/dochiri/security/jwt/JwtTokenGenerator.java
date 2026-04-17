package com.dochiri.security.jwt;

import java.time.Instant;
import java.util.UUID;

public class JwtTokenGenerator {

    private final JwtProvider jwtProvider;

    public JwtTokenGenerator(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    public JwtTokenResult generateToken(String publicId, String role) {
        String sessionId = UUID.randomUUID().toString();
        String accessToken = jwtProvider.generateAccessToken(publicId, role, sessionId);
        String refreshToken = jwtProvider.generateRefreshToken(publicId, role, sessionId);
        Instant refreshExpiresAt = jwtProvider.extractExpiration(jwtProvider.parseAndValidate(refreshToken));

        return new JwtTokenResult(accessToken, refreshToken, refreshExpiresAt);
    }

    public String generateAccessToken(String publicId, String role) {
        return jwtProvider.generateAccessToken(publicId, role);
    }
}
