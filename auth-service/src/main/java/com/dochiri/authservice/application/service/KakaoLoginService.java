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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoLoginService implements KakaoLoginUseCase {

    private final KakaoOAuthPort kakaoOAuthPort;
    private final SocialUserCreatePort socialUserCreatePort;
    private final AuthAccountRepository authAccountRepository;
    private final AuthTokenIssueUseCase authTokenIssueUseCase;

    @Transactional
    @Override
    public IssueAuthTokenResult login(KakaoLoginCommand command) {
        KakaoUserProfileResult profile = kakaoOAuthPort.authenticate(new KakaoAuthenticationCommand(command.code()));
        String providerUserId = String.valueOf(profile.id());

        AuthAccount authAccount = authAccountRepository.findByProviderAndProviderId(
                        AuthProvider.KAKAO.name(),
                        providerUserId
                )
                .orElseGet(() -> provisionSocialAccount(profile));

        return authTokenIssueUseCase.issue(new IssueAuthTokenCommand(authAccount.userId(), authAccount.publicId(), authAccount.role()));
    }

    private AuthAccount provisionSocialAccount(KakaoUserProfileResult profile) {
        CreateSocialUserResult createdUser = socialUserCreatePort.create(new CreateSocialUserCommand(
                profile.nickname(),
                profile.profileImageUrl()
        ));

        return authAccountRepository.save(new AuthAccount(
                createdUser.userId(),
                createdUser.publicId(),
                AuthProvider.KAKAO,
                String.valueOf(profile.id()),
                UserRole.USER
        ));
    }

}
