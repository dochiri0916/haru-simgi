package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.application.service.ReissueTokenService;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.authservice.domain.RefreshToken;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.properties.JwtProperties;
import com.dochiri.security.jwt.JwtProvider;
import com.dochiri.security.jwt.JwtTokenGenerator;
import com.dochiri.security.role.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReissueTokenServiceTest {

    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final JwtProvider jwtProvider = new JwtProvider(new JwtProperties(
            "12345678901234567890123456789012",
            1_800_000L,
            1_209_600_000L
    ));
    private final JwtTokenGenerator jwtTokenGenerator = new JwtTokenGenerator(jwtProvider);

    private ReissueTokenService reissueTokenService;

    @BeforeEach
    void setUp() {
        reissueTokenService = new ReissueTokenService(jwtProvider, jwtTokenGenerator, authAccountRepository, refreshTokenRepository);
    }

    @Test
    void 저장된_리프레시_토큰이면_토큰을_재발급한다() {
        String refreshToken = jwtProvider.generateRefreshToken(1L, "USER");
        var claims = jwtProvider.parseAndValidate(refreshToken);
        String tokenId = jwtProvider.extractTokenId(claims);

        when(refreshTokenRepository.findByTokenId(tokenId))
                .thenReturn(Optional.of(RefreshToken.create(tokenId, 1L, Instant.now().plusSeconds(60))));
        when(authAccountRepository.findByUserId(1L))
                .thenReturn(Optional.of(new AuthAccount(1L, AuthProvider.LOCAL, null, "alice@example.com", "password-hash", UserRole.USER)));
        when(refreshTokenRepository.replaceByUserId(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = reissueTokenService.reissue(new RefreshTokenCommand(refreshToken));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.role()).isEqualTo(UserRole.USER);
        verify(refreshTokenRepository).replaceByUserId(any(RefreshToken.class));
    }

    @Test
    void 저장되지_않은_리프레시_토큰이면_예외가_발생한다() {
        String refreshToken = jwtProvider.generateRefreshToken(1L, "USER");
        var claims = jwtProvider.parseAndValidate(refreshToken);
        String tokenId = jwtProvider.extractTokenId(claims);

        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reissueTokenService.reissue(new RefreshTokenCommand(refreshToken)))
                .isInstanceOf(BaseException.class);
    }
}
