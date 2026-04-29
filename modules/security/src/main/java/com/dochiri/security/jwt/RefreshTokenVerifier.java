package com.dochiri.security.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;

public class RefreshTokenVerifier {

    private final JwtProvider jwtProvider;

    public RefreshTokenVerifier(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    public String verifyAndExtractPublicId(String refreshToken) {
        return verify(refreshToken).publicId();
    }

    public RefreshTokenInfo verify(String refreshToken) {
        Claims claims = jwtProvider.parseAndValidate(refreshToken);

        if (!jwtProvider.isRefreshToken(claims)) {
            throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다.");
        }

        return new RefreshTokenInfo(
                jwtProvider.extractPublicId(claims),
                jwtProvider.extractTokenId(claims)
        );
    }

}
