package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.KakaoLoginUseCase;
import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.out.KakaoOAuthPort;
import com.dochiri.authservice.application.port.out.SocialUserProvisionPort;
import com.dochiri.authservice.application.port.out.dto.KakaoUserProfile;
import com.dochiri.authservice.application.port.out.dto.ProvisionedSocialUser;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.security.role.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoLoginService implements KakaoLoginUseCase {

    private final KakaoOAuthPort kakaoOAuthPort;
    private final SocialUserProvisionPort socialUserProvisionPort;
    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenIssuer authTokenIssuer;

    @Override
    public String buildAuthorizeUrl(String state) {
        return kakaoOAuthPort.buildAuthorizeUrl(state);
    }

    @Transactional
    @Override
    public AuthTokenResult login(KakaoLoginCommand command) {
        KakaoUserProfile profile = kakaoOAuthPort.authenticate(command.code());
        String providerUserId = String.valueOf(profile.id());

        AuthAccount authAccount = authAccountRepository.findByProviderAndProviderUserId(
                        AuthProvider.KAKAO.name(),
                        providerUserId
                )
                .orElseGet(() -> provisionSocialAccount(profile));

        return authTokenIssuer.issue(authAccount);
    }

    private AuthAccount provisionSocialAccount(KakaoUserProfile profile) {
        ProvisionedSocialUser provisionedUser = socialUserProvisionPort.provision(
                profile.email(),
                profile.nickname(),
                profile.profileImageUrl()
        );

        return authAccountRepository.save(new AuthAccount(
                provisionedUser.userId(),
                AuthProvider.KAKAO,
                String.valueOf(profile.id()),
                provisionedUser.email(),
                passwordEncoder.encode("kakao:" + profile.id() + ":" + UUID.randomUUID()),
                UserRole.USER
        ));
    }

}
