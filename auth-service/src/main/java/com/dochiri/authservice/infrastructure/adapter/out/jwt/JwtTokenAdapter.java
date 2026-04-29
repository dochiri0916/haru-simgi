package com.dochiri.authservice.infrastructure.adapter.out.jwt;

import com.dochiri.authservice.application.port.out.TokenGeneratePort;
import com.dochiri.authservice.application.port.out.TokenParsePort;
import com.dochiri.authservice.application.port.out.dto.IssuedTokenResult;
import com.dochiri.authservice.application.port.out.dto.ParseRefreshTokenResult;
import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.jwt.JwtTokenGenerator;
import com.dochiri.security.jwt.JwtTokenResult;
import com.dochiri.security.jwt.RefreshTokenInfo;
import com.dochiri.security.jwt.RefreshTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenGeneratePort, TokenParsePort {

    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenVerifier refreshTokenVerifier;

    @Override
    public IssuedTokenResult generate(String publicId, String role) {
        JwtTokenResult tokenResult = jwtTokenGenerator.generateToken(publicId, role);
        return new IssuedTokenResult(
                tokenResult.accessToken(),
                tokenResult.refreshToken(),
                tokenResult.refreshTokenId(),
                tokenResult.refreshTokenExpiresAt()
        );
    }

    @Override
    public ParseRefreshTokenResult parseRefreshToken(String token) {
        try {
            RefreshTokenInfo info = refreshTokenVerifier.verify(token);
            return new ParseRefreshTokenResult(info.publicId(), info.tokenId());
        } catch (BadCredentialsException exception) {
            throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN, exception);
        }
    }
}
