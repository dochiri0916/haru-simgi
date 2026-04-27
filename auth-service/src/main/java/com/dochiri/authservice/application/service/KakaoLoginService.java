package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.AuthTokenIssueUseCase;
import com.dochiri.authservice.application.port.in.KakaoLoginUseCase;
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
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.security.role.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoLoginService implements KakaoLoginUseCase {

    private final KakaoOAuthPort kakaoOAuthPort;
    private final SocialUserCreatePort socialUserCreatePort;
    private final AuthAccountRepository authAccountRepository;
    private final AuthTokenIssueUseCase authTokenIssueUseCase;

    @Override
    public IssueAuthTokenResult execute(KakaoLoginCommand command) {
        KakaoUserProfileResult profile = kakaoOAuthPort.authenticate(new KakaoAuthenticationCommand(command.code()));
        String providerUserId = String.valueOf(profile.id());

        AuthAccount authAccount = authAccountRepository.findByProviderAndProviderId(
                        AuthProvider.KAKAO,
                        providerUserId
                )
                .orElseGet(() -> provisionSocialAccount(profile, providerUserId));

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

}
