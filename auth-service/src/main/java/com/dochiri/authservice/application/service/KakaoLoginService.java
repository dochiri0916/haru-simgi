package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.AuthTokenIssueUseCase;
import com.dochiri.authservice.application.port.in.KakaoLoginUseCase;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.out.GuestHabitMigrationPort;
import com.dochiri.authservice.application.port.out.GuestSessionRepository;
import com.dochiri.authservice.application.port.out.GuestSessionTokenPort;
import com.dochiri.authservice.application.port.out.KakaoOAuthPort;
import com.dochiri.authservice.application.port.out.SocialUserCreatePort;
import com.dochiri.authservice.application.port.out.dto.CreateSocialUserCommand;
import com.dochiri.authservice.application.port.out.dto.CreateSocialUserResult;
import com.dochiri.authservice.application.port.out.dto.KakaoAuthenticationCommand;
import com.dochiri.authservice.application.port.out.dto.KakaoUserProfileResult;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.authservice.domain.GuestSession;
import com.dochiri.security.role.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class KakaoLoginService implements KakaoLoginUseCase {

    private final KakaoOAuthPort kakaoOAuthPort;
    private final SocialUserCreatePort socialUserCreatePort;
    private final AuthAccountRepository authAccountRepository;
    private final AuthTokenIssueUseCase authTokenIssueUseCase;
    private final GuestSessionRepository guestSessionRepository;
    private final GuestSessionTokenPort guestSessionTokenPort;
    private final GuestHabitMigrationPort guestHabitMigrationPort;
    private final Clock clock;

    @Transactional
    @Override
    public IssueAuthTokenResult execute(KakaoLoginCommand command) {
        KakaoUserProfileResult profile = kakaoOAuthPort.authenticate(new KakaoAuthenticationCommand(command.code()));
        String providerUserId = String.valueOf(profile.id());

        AuthAccount authAccount = authAccountRepository.findByProviderAndProviderId(
                        AuthProvider.KAKAO,
                        providerUserId
                )
                .orElseGet(() -> provisionSocialAccount(profile, providerUserId));

        migrateGuestHabitsIfPresent(command.guestSessionToken(), authAccount.publicId());

        return authTokenIssueUseCase.execute(new IssueAuthTokenCommand(authAccount.publicId(), authAccount.role()));
    }

    private AuthAccount provisionSocialAccount(KakaoUserProfileResult profile, String providerUserId) {
        String idempotencyKey = idempotencyKey(AuthProvider.KAKAO, providerUserId);
        CreateSocialUserResult createdUser = socialUserCreatePort.create(new CreateSocialUserCommand(
                idempotencyKey,
                profile.nickname(),
                profile.profileImageUrl()
        ));

        return authAccountRepository.save(new AuthAccount(
                createdUser.publicId(),
                AuthProvider.KAKAO,
                providerUserId,
                UserRole.USER
        ));
    }

    private String idempotencyKey(AuthProvider provider, String providerId) {
        return provider.name() + ":" + providerId;
    }

    private void migrateGuestHabitsIfPresent(String guestSessionToken, String userPublicId) {
        if (!StringUtils.hasText(guestSessionToken)) {
            return;
        }

        Instant now = clock.instant();
        guestSessionRepository.findByTokenHash(guestSessionTokenPort.hash(guestSessionToken))
                .filter(session -> session.isActiveAt(now))
                .ifPresent(session -> migrateGuestHabits(session, userPublicId, now));
    }

    private void migrateGuestHabits(GuestSession guestSession, String userPublicId, Instant now) {
        guestHabitMigrationPort.migrate(guestSession.publicId(), userPublicId);
        guestSessionRepository.save(guestSession.linkToUser(userPublicId, now));
    }

}
