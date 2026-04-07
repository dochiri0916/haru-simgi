package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.RefreshToken;
import com.dochiri.security.jwt.JwtProvider;
import com.dochiri.security.jwt.JwtTokenGenerator;
import com.dochiri.security.jwt.JwtTokenResult;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthTokenIssuer {

    private final JwtTokenGenerator jwtTokenGenerator;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthTokenResult issue(AuthAccount account) {
        JwtTokenResult tokenResult = jwtTokenGenerator.generateToken(account.userId(), account.role().name());
        storeRefreshToken(tokenResult);
        return AuthTokenResult.from(tokenResult, account.role());
    }

    private void storeRefreshToken(JwtTokenResult tokenResult) {
        Claims claims = jwtProvider.parseAndValidate(tokenResult.refreshToken());
        RefreshToken refreshToken = RefreshToken.create(
                jwtProvider.extractTokenId(claims),
                jwtProvider.extractUserId(claims),
                jwtProvider.extractExpiration(claims)
        );

        refreshTokenRepository.replaceByUserId(refreshToken);
    }

}
