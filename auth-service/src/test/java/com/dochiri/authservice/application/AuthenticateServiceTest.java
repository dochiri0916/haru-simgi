package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.dto.LoginCommand;
import com.dochiri.authservice.application.port.out.AuthUserRepository;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.domain.AuthUser;
import com.dochiri.authservice.domain.RefreshToken;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.configuration.properties.JwtProperties;
import com.dochiri.security.jwt.JwtProvider;
import com.dochiri.security.jwt.JwtTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticateServiceTest {

    private final AuthUserRepository authUserRepository = mock(AuthUserRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtProvider jwtProvider = new JwtProvider(new JwtProperties(
            "12345678901234567890123456789012",
            1_800_000L,
            1_209_600_000L
    ));
    private final JwtTokenGenerator jwtTokenGenerator = new JwtTokenGenerator(jwtProvider);

    private AuthenticateService authenticateService;

    @BeforeEach
    void setUp() {
        authenticateService = new AuthenticateService(
                authUserRepository,
                passwordEncoder,
                jwtTokenGenerator,
                jwtProvider,
                refreshTokenRepository
        );
    }

    @Test
    void 로그인에_성공하면_토큰을_발급하고_리프레시_토큰을_저장한다() {
        String passwordHash = passwordEncoder.encode("secret123");
        when(authUserRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(new AuthUser(1L, "user-public-id", "alice@example.com", passwordHash, "USER")));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = authenticateService.authenticate(new LoginCommand("alice@example.com", "secret123"));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.refreshTokenExpiresAt()).isAfter(Instant.now().minusSeconds(1));
        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void 비밀번호가_틀리면_예외가_발생한다() {
        when(authUserRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(new AuthUser(
                        1L,
                        "user-public-id",
                        "alice@example.com",
                        passwordEncoder.encode("secret123"),
                        "USER"
                )));

        assertThatThrownBy(() -> authenticateService.authenticate(new LoginCommand("alice@example.com", "wrong-password")))
                .isInstanceOf(BaseException.class);
    }
}
