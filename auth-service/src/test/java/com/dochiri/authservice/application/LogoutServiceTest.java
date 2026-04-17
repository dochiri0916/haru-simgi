package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.dto.LogoutCommand;
import com.dochiri.authservice.application.port.out.AuthSessionRepository;
import com.dochiri.authservice.application.service.LogoutService;
import com.dochiri.authservice.domain.AuthSession;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.jwt.JwtProvider;
import com.dochiri.security.properties.JwtProperties;
import com.dochiri.security.role.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LogoutServiceTest {

    private final AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
    private final JwtProvider jwtProvider = new JwtProvider(new JwtProperties(
            "12345678901234567890123456789012",
            1_800_000L,
            1_209_600_000L
    ));

    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutService(jwtProvider, authSessionRepository);
    }

    @Test
    void 저장된_리프레시_토큰이면_로그아웃에_성공한다() {
        String refreshToken = jwtProvider.generateRefreshToken("public-id-1", "USER");
        var claims = jwtProvider.parseAndValidate(refreshToken);
        String tokenId = jwtProvider.extractTokenId(claims);

        when(authSessionRepository.findByRefreshTokenId(tokenId))
                .thenReturn(Optional.of(AuthSession.create(
                        tokenId,
                        1L,
                        "public-id-1",
                        UserRole.USER,
                        Instant.parse("2026-04-17T00:00:00Z"),
                        Instant.parse("2026-04-17T00:01:00Z")
                )));

        logoutService.logout(new LogoutCommand(refreshToken));

        verify(authSessionRepository).deleteBySessionId(tokenId);
    }

    @Test
    void 저장되지_않은_리프레시_토큰이면_예외가_발생한다() {
        String refreshToken = jwtProvider.generateRefreshToken("public-id-1", "USER");
        var claims = jwtProvider.parseAndValidate(refreshToken);
        String tokenId = jwtProvider.extractTokenId(claims);

        when(authSessionRepository.findByRefreshTokenId(tokenId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> logoutService.logout(new LogoutCommand(refreshToken)))
                .isInstanceOf(BaseException.class);
    }
}
