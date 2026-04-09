package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.KakaoLoginUseCase;
import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoLoginService implements KakaoLoginUseCase {

    private final KakaoOAuthPort kakaoOAuthPort;
    private final SocialUserCreatePort socialUserCreatePort;
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
        KakaoUserProfileResult profile = kakaoOAuthPort.authenticate(new KakaoAuthenticationCommand(command.code()));
        String providerUserId = String.valueOf(profile.id());

        AuthAccount authAccount = authAccountRepository.findByProviderAndProviderUserId(
                        AuthProvider.KAKAO.name(),
                        providerUserId
                )
                .orElseGet(() -> provisionSocialAccount(profile));

        return authTokenIssuer.issue(authAccount);
    }

    private AuthAccount provisionSocialAccount(KakaoUserProfileResult profile) {
        CreateSocialUserResult createdUser = socialUserCreatePort.create(new CreateSocialUserCommand(
                profile.email(),
                profile.nickname(),
                profile.profileImageUrl()
        ));

        return authAccountRepository.save(new AuthAccount(
                createdUser.userId(),
                AuthProvider.KAKAO,
                String.valueOf(profile.id()),
                createdUser.email(),
                passwordEncoder.encode("kakao:" + profile.id() + ":" + UUID.randomUUID()),
                UserRole.USER
        ));
    }

}
