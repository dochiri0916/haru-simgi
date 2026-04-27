package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.dto.LogoutCommand;
import com.dochiri.authservice.application.port.out.AuthSessionRepository;
import com.dochiri.authservice.application.port.out.TokenParsePort;
import com.dochiri.authservice.application.port.out.dto.ParseRefreshTokenResult;
import com.dochiri.authservice.application.service.LogoutService;
import com.dochiri.authservice.domain.AuthSession;
import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.role.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

class LogoutServiceTest {

    private final AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
    private final TokenParsePort tokenParsePort = mock(TokenParsePort.class);

    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutService(tokenParsePort, authSessionRepository);
    }

    @Test
    void 저장된_리프레시_토큰이면_세션을_삭제한다() {
        String refreshToken = "refresh-token";
        String tokenId = "session-id";

        when(tokenParsePort.parseRefreshToken(refreshToken))
                .thenReturn(new ParseRefreshTokenResult("public-id-1", tokenId));
        when(authSessionRepository.findByRefreshTokenId(tokenId))
                .thenReturn(Optional.of(AuthSession.create(
                        tokenId,
                        "public-id-1",
                        UserRole.USER,
                        Instant.parse("2026-04-17T00:00:00Z"),
                        Instant.parse("2026-04-17T00:01:00Z")
                )));

        logoutService.execute(new LogoutCommand(refreshToken));

        verify(authSessionRepository).deleteBySessionId(tokenId);
    }

    @Test
    void 세션이_이미_없어도_조용히_무시한다() {
        String refreshToken = "refresh-token";
        String tokenId = "session-id";

        when(tokenParsePort.parseRefreshToken(refreshToken))
                .thenReturn(new ParseRefreshTokenResult("public-id-1", tokenId));
        when(authSessionRepository.findByRefreshTokenId(tokenId)).thenReturn(Optional.empty());

        logoutService.execute(new LogoutCommand(refreshToken));

        verify(authSessionRepository, never()).deleteBySessionId(anyString());
    }

    @Test
    void 유효하지_않은_리프레시_토큰이면_조용히_무시한다() {
        String refreshToken = "invalid-token";

        when(tokenParsePort.parseRefreshToken(refreshToken))
                .thenThrow(new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        logoutService.execute(new LogoutCommand(refreshToken));

        verifyNoInteractions(authSessionRepository);
    }
}
