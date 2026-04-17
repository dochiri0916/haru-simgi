package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.application.port.out.AuthSessionRepository;
import com.dochiri.authservice.application.port.out.TokenGeneratePort;
import com.dochiri.authservice.application.port.out.dto.IssuedTokenResult;
import com.dochiri.authservice.application.service.AuthTokenIssuerService;
import com.dochiri.authservice.domain.AuthSession;
import com.dochiri.security.role.UserRole;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthTokenIssuerServiceTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-04-17T00:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-04-17T01:00:00Z");

    private final TokenGeneratePort tokenGeneratePort = mock(TokenGeneratePort.class);
    private final AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
    private final Clock clock = Clock.fixed(ISSUED_AT, ZoneId.of("UTC"));
    private final AuthTokenIssuerService authTokenIssuerService = new AuthTokenIssuerService(
            tokenGeneratePort,
            authSessionRepository,
            clock
    );

    @Test
    void 토큰을_발급할_때_세션_발급시각은_주입된_Clock을_사용한다() {
        when(tokenGeneratePort.generate("public-id-1", "USER"))
                .thenReturn(new IssuedTokenResult(
                        "access-token",
                        "refresh-token",
                        "refresh-token-id",
                        EXPIRES_AT
                ));

        var result = authTokenIssuerService.issue(new IssueAuthTokenCommand(1L, "public-id-1", UserRole.USER));

        ArgumentCaptor<AuthSession> authSessionCaptor = ArgumentCaptor.forClass(AuthSession.class);
        verify(authSessionRepository).saveReplacingUserSessions(authSessionCaptor.capture());
        AuthSession authSession = authSessionCaptor.getValue();

        assertThat(authSession.issuedAt()).isEqualTo(ISSUED_AT);
        assertThat(authSession.expiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }
}
