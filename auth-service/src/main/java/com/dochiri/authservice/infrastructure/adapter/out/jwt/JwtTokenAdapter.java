package com.dochiri.authservice.infrastructure.adapter.out.jwt;

import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.authservice.application.port.out.TokenGeneratePort;
import com.dochiri.authservice.application.port.out.TokenParsePort;
import com.dochiri.authservice.application.port.out.dto.IssuedTokenResult;
import com.dochiri.authservice.application.port.out.dto.ParseRefreshTokenResult;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.jwt.JwtProvider;
import com.dochiri.security.jwt.JwtTokenGenerator;
import com.dochiri.security.jwt.JwtTokenResult;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenGeneratePort, TokenParsePort {

    private final JwtTokenGenerator jwtTokenGenerator;
    private final JwtProvider jwtProvider;

    @Override
    public IssuedTokenResult generate(String publicId, String role) {
        JwtTokenResult tokenResult = jwtTokenGenerator.generateToken(publicId, role);
        Claims claims = jwtProvider.parseAndValidate(tokenResult.refreshToken());
        return new IssuedTokenResult(
                tokenResult.accessToken(),
                tokenResult.refreshToken(),
                jwtProvider.extractTokenId(claims),
                jwtProvider.extractExpiration(claims)
        );
    }

    @Override
    public ParseRefreshTokenResult parseRefreshToken(String token) {
        Claims claims = jwtProvider.parseAndValidate(token);
        if (!jwtProvider.isRefreshToken(claims)) {
            throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        return new ParseRefreshTokenResult(
                jwtProvider.extractPublicId(claims),
                jwtProvider.extractTokenId(claims)
        );
    }

}
