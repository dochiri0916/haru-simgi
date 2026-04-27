package com.dochiri.authservice.application.service;

import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.authservice.application.port.in.LogoutUseCase;
import com.dochiri.authservice.application.port.in.dto.LogoutCommand;
import com.dochiri.authservice.application.port.out.AuthSessionRepository;
import com.dochiri.authservice.application.port.out.TokenParsePort;
import com.dochiri.authservice.application.port.out.dto.ParseRefreshTokenResult;
import com.dochiri.errorhandling.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final TokenParsePort tokenParsePort;
    private final AuthSessionRepository authSessionRepository;

    @Transactional
    @Override
    public void execute(LogoutCommand command) {
        ParseRefreshTokenResult parsed;
        try {
            parsed = tokenParsePort.parseRefreshToken(command.refreshToken());
        } catch (BaseException exception) {
            if (exception.getErrorCode() == AuthErrorCode.INVALID_REFRESH_TOKEN) {
                return;
            }
            throw exception;
        }

        authSessionRepository.findByRefreshTokenId(parsed.tokenId())
                .ifPresent(session -> authSessionRepository.deleteBySessionId(session.sessionId()));
    }
}
