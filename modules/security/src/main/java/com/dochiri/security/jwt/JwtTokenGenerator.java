package com.dochiri.security.jwt;

import io.jsonwebtoken.Claims;

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
        Claims refreshClaims = jwtProvider.parseAndValidate(refreshToken);

        return new JwtTokenResult(
                accessToken,
                refreshToken,
                jwtProvider.extractTokenId(refreshClaims),
                jwtProvider.extractExpiration(refreshClaims)
        );
    }

    public String generateAccessToken(String publicId, String role) {
        return jwtProvider.generateAccessToken(publicId, role);
    }
}
