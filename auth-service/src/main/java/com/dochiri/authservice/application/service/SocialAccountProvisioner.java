package com.dochiri.authservice.application.service;

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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SocialAccountProvisioner {

    private final KakaoOAuthPort kakaoOAuthPort;
    private final SocialUserCreatePort socialUserCreatePort;
    private final AuthAccountRepository authAccountRepository;

    @Transactional
    public AuthAccount authenticateKakao(String authorizationCode) {
        KakaoUserProfileResult profile = kakaoOAuthPort.authenticate(new KakaoAuthenticationCommand(authorizationCode));
        String providerUserId = String.valueOf(profile.id());

        return authAccountRepository.findByProviderAndProviderId(AuthProvider.KAKAO, providerUserId)
                .orElseGet(() -> provision(profile, providerUserId));
    }

    private AuthAccount provision(KakaoUserProfileResult profile, String providerUserId) {
        String idempotencyKey = AuthProvider.KAKAO.name() + ":" + providerUserId;
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
}
