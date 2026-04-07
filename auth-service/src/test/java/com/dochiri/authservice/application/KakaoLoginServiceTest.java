package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.out.KakaoOAuthPort;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.application.port.out.SocialUserProvisionPort;
import com.dochiri.authservice.application.port.out.dto.KakaoUserProfile;
import com.dochiri.authservice.application.port.out.dto.ProvisionedSocialUser;
import com.dochiri.authservice.application.service.AuthTokenIssuer;
import com.dochiri.authservice.application.service.KakaoLoginService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KakaoLoginServiceTest {

    private final KakaoOAuthPort kakaoOAuthPort = mock(KakaoOAuthPort.class);
    private final SocialUserProvisionPort socialUserProvisionPort = mock(SocialUserProvisionPort.class);
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

    private KakaoLoginService kakaoLoginService;

    @BeforeEach
    void setUp() {
        kakaoLoginService = new KakaoLoginService(
                kakaoOAuthPort,
                socialUserProvisionPort,
                authAccountRepository,
                passwordEncoder,
                authTokenIssuer
        );
        when(refreshTokenRepository.replaceByUserId(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void 기존_카카오_계정이_있으면_그대로_로그인한다() {
        when(kakaoOAuthPort.authenticate("auth-code"))
                .thenReturn(new KakaoUserProfile(
                        100L,
                        "alice@example.com",
                        "alice",
                        "https://example.com/alice.png"
                ));
        when(authAccountRepository.findByProviderAndProviderUserId("KAKAO", "100"))
                .thenReturn(java.util.Optional.of(new AuthAccount(
                        1L,
                        AuthProvider.KAKAO,
                        "100",
                        "alice@example.com",
                        passwordEncoder.encode("secret123"),
                        UserRole.USER
                )));

        var result = kakaoLoginService.login(new KakaoLoginCommand("auth-code"));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        verify(socialUserProvisionPort, never()).provision(anyString(), anyString(), anyString());
        verify(authAccountRepository, never()).save(any());
    }

    @Test
    void 처음_카카오_로그인한_사용자면_이메일이_없어도_인증_계정을_생성한다() {
        when(kakaoOAuthPort.authenticate("auth-code"))
                .thenReturn(new KakaoUserProfile(
                        200L,
                        null,
                        "kakao-user",
                        "https://example.com/profile.png"
                ));
        when(authAccountRepository.findByProviderAndProviderUserId("KAKAO", "200"))
                .thenReturn(java.util.Optional.empty());
        when(socialUserProvisionPort.provision(null, "kakao-user", "https://example.com/profile.png"))
                .thenReturn(new ProvisionedSocialUser(7L, null, "kakao-user", "https://example.com/profile.png"));
        when(authAccountRepository.save(any(AuthAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = kakaoLoginService.login(new KakaoLoginCommand("auth-code"));

        ArgumentCaptor<AuthAccount> authAccountCaptor = ArgumentCaptor.forClass(AuthAccount.class);
        verify(authAccountRepository).save(authAccountCaptor.capture());

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(authAccountCaptor.getValue().userId()).isEqualTo(7L);
        assertThat(authAccountCaptor.getValue().provider()).isEqualTo(AuthProvider.KAKAO);
        assertThat(authAccountCaptor.getValue().providerUserId()).isEqualTo("200");
        assertThat(authAccountCaptor.getValue().email()).isNull();
        assertThat(authAccountCaptor.getValue().role()).isEqualTo(UserRole.USER);
        assertThat(authAccountCaptor.getValue().passwordHash()).isNotBlank();
    }
}
