package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.AuthTokenIssueUseCase;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.out.KakaoOAuthPort;
import com.dochiri.authservice.application.port.out.SocialUserCreatePort;
import com.dochiri.authservice.application.port.out.dto.CreateSocialUserCommand;
import com.dochiri.authservice.application.port.out.dto.CreateSocialUserResult;
import com.dochiri.authservice.application.port.out.dto.KakaoAuthenticationCommand;
import com.dochiri.authservice.application.port.out.dto.KakaoUserProfileResult;
import com.dochiri.authservice.application.service.KakaoLoginService;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.security.role.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KakaoLoginServiceTest {

    private final KakaoOAuthPort kakaoOAuthPort = mock(KakaoOAuthPort.class);
    private final SocialUserCreatePort socialUserCreatePort = mock(SocialUserCreatePort.class);
    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);
    private final AuthTokenIssueUseCase authTokenIssueUseCase = mock(AuthTokenIssueUseCase.class);

    private KakaoLoginService kakaoLoginService;

    @BeforeEach
    void setUp() {
        kakaoLoginService = new KakaoLoginService(
                kakaoOAuthPort,
                socialUserCreatePort,
                authAccountRepository,
                authTokenIssueUseCase
        );
        when(authTokenIssueUseCase.execute(any(IssueAuthTokenCommand.class)))
                .thenReturn(new IssueAuthTokenResult("access-token", "refresh-token", Instant.now().plusSeconds(3600), UserRole.USER));
    }

    @Test
    void 기존_카카오_계정이_있으면_그대로_로그인한다() {
        when(kakaoOAuthPort.authenticate(new KakaoAuthenticationCommand("auth-code")))
                .thenReturn(new KakaoUserProfileResult(
                        100L,
                        "alice@example.com",
                        "alice",
                        "https://example.com/alice.png"
                ));
        when(authAccountRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "100"))
                .thenReturn(java.util.Optional.of(new AuthAccount(
                        "public-id-1",
                        AuthProvider.KAKAO,
                        "100",
                        UserRole.USER
                )));

        var result = kakaoLoginService.execute(new KakaoLoginCommand("auth-code"));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        verify(socialUserCreatePort, never()).create(any());
        verify(authAccountRepository, never()).save(any());
    }

    @Test
    void 처음_카카오_로그인한_사용자면_이메일이_없어도_인증_계정을_생성한다() {
        when(kakaoOAuthPort.authenticate(new KakaoAuthenticationCommand("auth-code")))
                .thenReturn(new KakaoUserProfileResult(
                        200L,
                        null,
                        "kakao-user",
                        "https://example.com/profile.png"
                ));
        when(authAccountRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "200"))
                .thenReturn(java.util.Optional.empty());
        when(socialUserCreatePort.create(new CreateSocialUserCommand("KAKAO:200", "kakao-user", "https://example.com/profile.png")))
                .thenReturn(new CreateSocialUserResult("public-id-7", "kakao-user", "https://example.com/profile.png"));
        when(authAccountRepository.save(any(AuthAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = kakaoLoginService.execute(new KakaoLoginCommand("auth-code"));

        ArgumentCaptor<AuthAccount> authAccountCaptor = ArgumentCaptor.forClass(AuthAccount.class);
        verify(authAccountRepository).save(authAccountCaptor.capture());

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(authAccountCaptor.getValue().publicId()).isEqualTo("public-id-7");
        assertThat(authAccountCaptor.getValue().provider()).isEqualTo(AuthProvider.KAKAO);
        assertThat(authAccountCaptor.getValue().providerId()).isEqualTo("200");
        assertThat(authAccountCaptor.getValue().role()).isEqualTo(UserRole.USER);
    }
}
