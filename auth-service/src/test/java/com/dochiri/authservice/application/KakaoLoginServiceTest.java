package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.AuthTokenIssueUseCase;
import com.dochiri.authservice.application.port.in.dto.GuestMergeStatus;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginResult;
import com.dochiri.authservice.application.service.GuestHabitMigrationCoordinator;
import com.dochiri.authservice.application.service.KakaoLoginService;
import com.dochiri.authservice.application.service.SocialAccountProvisioner;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.security.role.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KakaoLoginServiceTest {

    private final SocialAccountProvisioner socialAccountProvisioner = mock(SocialAccountProvisioner.class);
    private final GuestHabitMigrationCoordinator guestHabitMigrationCoordinator = mock(GuestHabitMigrationCoordinator.class);
    private final AuthTokenIssueUseCase authTokenIssueUseCase = mock(AuthTokenIssueUseCase.class);

    private KakaoLoginService kakaoLoginService;

    @BeforeEach
    void setUp() {
        kakaoLoginService = new KakaoLoginService(
                socialAccountProvisioner,
                guestHabitMigrationCoordinator,
                authTokenIssueUseCase
        );
        when(authTokenIssueUseCase.execute(any(IssueAuthTokenCommand.class)))
                .thenReturn(new IssueAuthTokenResult("access-token", "refresh-token", Instant.now().plusSeconds(3600), UserRole.USER));
    }

    @Test
    void 게스트_세션_없이_로그인하면_SKIPPED_상태가_반환된다() {
        when(socialAccountProvisioner.authenticateKakao("auth-code"))
                .thenReturn(new AuthAccount("user-public-id", AuthProvider.KAKAO, "100", UserRole.USER));
        when(guestHabitMigrationCoordinator.migrate(null, "user-public-id"))
                .thenReturn(GuestMergeStatus.SKIPPED);

        KakaoLoginResult result = kakaoLoginService.execute(new KakaoLoginCommand("auth-code"));

        assertThat(result.tokens().accessToken()).isEqualTo("access-token");
        assertThat(result.tokens().refreshToken()).isEqualTo("refresh-token");
        assertThat(result.guestMerge()).isEqualTo(GuestMergeStatus.SKIPPED);
    }

    @Test
    void 게스트_세션_쿠키가_있으면_머지_코디네이터를_호출한다() {
        when(socialAccountProvisioner.authenticateKakao("auth-code"))
                .thenReturn(new AuthAccount("user-public-id", AuthProvider.KAKAO, "100", UserRole.USER));
        when(guestHabitMigrationCoordinator.migrate("raw-guest-token", "user-public-id"))
                .thenReturn(GuestMergeStatus.SUCCEEDED);

        KakaoLoginResult result = kakaoLoginService.execute(new KakaoLoginCommand("auth-code", "raw-guest-token"));

        assertThat(result.guestMerge()).isEqualTo(GuestMergeStatus.SUCCEEDED);
        verify(guestHabitMigrationCoordinator).migrate("raw-guest-token", "user-public-id");
    }

    @Test
    void 머지가_실패해도_토큰은_정상_발급된다() {
        when(socialAccountProvisioner.authenticateKakao("auth-code"))
                .thenReturn(new AuthAccount("user-public-id", AuthProvider.KAKAO, "100", UserRole.USER));
        when(guestHabitMigrationCoordinator.migrate("raw-guest-token", "user-public-id"))
                .thenReturn(GuestMergeStatus.FAILED);

        KakaoLoginResult result = kakaoLoginService.execute(new KakaoLoginCommand("auth-code", "raw-guest-token"));

        assertThat(result.tokens().accessToken()).isEqualTo("access-token");
        assertThat(result.guestMerge()).isEqualTo(GuestMergeStatus.FAILED);
    }
}
