package com.dochiri.authservice.application;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.in.ReissueTokenUseCase;
import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.domain.RefreshToken;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.jwt.JwtProvider;
import com.dochiri.security.jwt.JwtTokenGenerator;
import com.dochiri.security.jwt.JwtTokenResult;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReissueTokenService implements ReissueTokenUseCase {

    private final JwtProvider jwtProvider;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Override
    public AuthTokenResult reissue(RefreshTokenCommand command) {
        Claims claims = jwtProvider.parseAndValidate(command.refreshToken());

        if (!jwtProvider.isRefreshToken(claims)) {
            throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.extractUserId(claims);
        String role = jwtProvider.extractRole(claims);
        String tokenId = jwtProvider.extractTokenId(claims);

        RefreshToken storedRefreshToken = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedRefreshToken.getUserId().equals(userId)) {
            throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        refreshTokenRepository.deleteByUserId(userId);

        JwtTokenResult tokenResult = jwtTokenGenerator.generateToken(userId, role);
        storeRefreshToken(tokenResult);

        return AuthTokenResult.from(tokenResult);
    }

    private void storeRefreshToken(JwtTokenResult tokenResult) {
        Claims claims = jwtProvider.parseAndValidate(tokenResult.refreshToken());
        refreshTokenRepository.save(
                RefreshToken.create(
                        jwtProvider.extractTokenId(claims),
                        jwtProvider.extractUserId(claims),
                        jwtProvider.extractExpiration(claims)
                )
        );
    }
}
