package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.in.ReissueTokenUseCase;
import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.domain.AuthAccount;
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
    private final AuthAccountRepository authAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Override
    public AuthTokenResult reissue(RefreshTokenCommand command) {
        Claims claims = jwtProvider.parseAndValidate(command.refreshToken());

        if (!jwtProvider.isRefreshToken(claims)) {
            throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.extractUserId(claims);
        String tokenId = jwtProvider.extractTokenId(claims);

        RefreshToken storedRefreshToken = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedRefreshToken.getUserId().equals(userId)) {
            throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        AuthAccount account = authAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND));

        refreshTokenRepository.deleteByUserId(userId);

        JwtTokenResult tokenResult = jwtTokenGenerator.generateToken(userId, account.role().name());
        storeRefreshToken(tokenResult);

        return AuthTokenResult.from(tokenResult, account.role());
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
