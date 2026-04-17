package com.dochiri.authservice.application.service;

import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.authservice.application.port.in.LogoutUseCase;
import com.dochiri.authservice.application.port.in.dto.LogoutCommand;
import com.dochiri.authservice.application.port.out.AuthSessionRepository;
import com.dochiri.authservice.domain.AuthSession;
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
    private final AuthSessionRepository authSessionRepository;

    @Transactional
    @Override
    public void logout(LogoutCommand command) {
        Claims claims = jwtProvider.parseAndValidate(command.refreshToken());

        if (!jwtProvider.isRefreshToken(claims)) {
            throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String tokenId = jwtProvider.extractTokenId(claims);

        AuthSession authSession = authSessionRepository.findByRefreshTokenId(tokenId)
                .orElseThrow(() -> new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        authSessionRepository.deleteBySessionId(authSession.sessionId());
    }
}
