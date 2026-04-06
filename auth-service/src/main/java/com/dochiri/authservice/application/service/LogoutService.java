package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.in.LogoutUseCase;
import com.dochiri.authservice.application.port.in.dto.LogoutCommand;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.domain.RefreshToken;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Override
    public void logout(LogoutCommand command) {
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

        refreshTokenRepository.deleteByUserId(userId);
    }
}
