package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.dto.RegisterCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.application.port.out.UserCreatePort;
import com.dochiri.authservice.application.port.out.dto.CreateUserCommand;
import com.dochiri.authservice.application.port.out.dto.CreateUserResult;
import com.dochiri.authservice.application.service.AuthTokenIssuer;
import com.dochiri.authservice.application.service.RegisterService;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.authservice.domain.RefreshToken;
import com.dochiri.security.jwt.JwtProvider;
import com.dochiri.security.jwt.JwtTokenGenerator;
import com.dochiri.security.properties.JwtProperties;
import com.dochiri.security.role.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterServiceTest {

    private final UserCreatePort userCreatePort = mock(UserCreatePort.class);
    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtProvider jwtProvider = new JwtProvider(new JwtProperties(
            "12345678901234567890123456789012",
            1_800_000L,
            1_209_600_000L
    ));
    private final JwtTokenGenerator jwtTokenGenerator = new JwtTokenGenerator(jwtProvider);
    private final AuthTokenIssuer authTokenIssuer =
            new AuthTokenIssuer(jwtTokenGenerator, jwtProvider, refreshTokenRepository);

    private RegisterService registerService;

    @BeforeEach
    void setUp() {
        registerService = new RegisterService(
                userCreatePort,
                authAccountRepository,
                passwordEncoder,
                authTokenIssuer
        );
        when(refreshTokenRepository.replaceByUserId(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void 회원가입에_성공하면_사용자를_생성하고_인증_계정과_토큰을_발급한다() {
        when(userCreatePort.create(new CreateUserCommand("alice@example.com")))
                .thenReturn(new CreateUserResult(1L, "alice@example.com"));
        when(authAccountRepository.save(any(AuthAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = registerService.register(new RegisterCommand("alice@example.com", "secret123"));

        ArgumentCaptor<AuthAccount> authAccountCaptor = ArgumentCaptor.forClass(AuthAccount.class);
        verify(authAccountRepository).save(authAccountCaptor.capture());

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.refreshTokenExpiresAt()).isAfter(Instant.now().minusSeconds(1));
        assertThat(authAccountCaptor.getValue().userId()).isEqualTo(1L);
        assertThat(authAccountCaptor.getValue().provider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(authAccountCaptor.getValue().providerUserId()).isNull();
        assertThat(authAccountCaptor.getValue().email()).isEqualTo("alice@example.com");
        assertThat(authAccountCaptor.getValue().role()).isEqualTo(UserRole.USER);
        assertThat(passwordEncoder.matches("secret123", authAccountCaptor.getValue().passwordHash())).isTrue();
    }

}
