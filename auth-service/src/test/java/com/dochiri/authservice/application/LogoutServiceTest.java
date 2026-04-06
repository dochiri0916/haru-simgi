package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.dto.LogoutCommand;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.application.service.LogoutService;
import com.dochiri.authservice.domain.RefreshToken;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.jwt.JwtProvider;
import com.dochiri.security.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogoutServiceTest {

    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final JwtProvider jwtProvider = new JwtProvider(new JwtProperties(
            "12345678901234567890123456789012",
            1_800_000L,
            1_209_600_000L
    ));

    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutService(jwtProvider, refreshTokenRepository);
    }

    @Test
    void 저장된_리프레시_토큰이면_로그아웃에_성공한다() {
        String refreshToken = jwtProvider.generateRefreshToken(1L, "USER");
        var claims = jwtProvider.parseAndValidate(refreshToken);
        String tokenId = jwtProvider.extractTokenId(claims);

        when(refreshTokenRepository.findByTokenId(tokenId))
                .thenReturn(Optional.of(RefreshToken.create(tokenId, 1L, Instant.now().plusSeconds(60))));

        logoutService.logout(new LogoutCommand(refreshToken));

        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    void 저장되지_않은_리프레시_토큰이면_예외가_발생한다() {
        String refreshToken = jwtProvider.generateRefreshToken(1L, "USER");
        var claims = jwtProvider.parseAndValidate(refreshToken);
        String tokenId = jwtProvider.extractTokenId(claims);

        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> logoutService.logout(new LogoutCommand(refreshToken)))
                .isInstanceOf(BaseException.class);
    }
}
