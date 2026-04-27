package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
import com.dochiri.authservice.application.port.out.AuthSessionRepository;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.out.TokenParsePort;
import com.dochiri.authservice.application.port.in.AuthTokenIssueUseCase;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.out.dto.ParseRefreshTokenResult;
import com.dochiri.authservice.application.service.ReissueTokenService;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.authservice.domain.AuthSession;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.role.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReissueTokenServiceTest {

    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);
    private final AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
    private final TokenParsePort tokenParsePort = mock(TokenParsePort.class);
    private final AuthTokenIssueUseCase authTokenIssueUseCase = mock(AuthTokenIssueUseCase.class);

    private ReissueTokenService reissueTokenService;

    @BeforeEach
    void setUp() {
        reissueTokenService = new ReissueTokenService(
                tokenParsePort,
                authTokenIssueUseCase,
                authAccountRepository,
                authSessionRepository
        );
    }

    @Test
    void 저장된_리프레시_토큰이면_토큰을_재발급한다() {
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
        when(authAccountRepository.findByPublicId("public-id-1"))
                .thenReturn(Optional.of(new AuthAccount("public-id-1", AuthProvider.KAKAO, "100", UserRole.USER)));
        when(authTokenIssueUseCase.execute(any(IssueAuthTokenCommand.class)))
                .thenReturn(new IssueAuthTokenResult(
                        "new-access-token",
                        "new-refresh-token",
                        Instant.now().plusSeconds(60),
                        UserRole.USER
                ));

        var result = reissueTokenService.execute(new RefreshTokenCommand(refreshToken));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.role()).isEqualTo(UserRole.USER);
        verify(authTokenIssueUseCase).execute(any(IssueAuthTokenCommand.class));
    }

    @Test
    void 저장되지_않은_리프레시_토큰이면_예외가_발생한다() {
        String refreshToken = "refresh-token";
        String tokenId = "session-id";

        when(tokenParsePort.parseRefreshToken(refreshToken))
                .thenReturn(new ParseRefreshTokenResult("public-id-1", tokenId));
        when(authSessionRepository.findByRefreshTokenId(tokenId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reissueTokenService.execute(new RefreshTokenCommand(refreshToken)))
                .isInstanceOf(BaseException.class);
    }
}
