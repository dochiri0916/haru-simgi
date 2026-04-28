package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.AuthTokenIssueUseCase;
import com.dochiri.authservice.application.port.in.KakaoLoginUseCase;
import com.dochiri.authservice.application.port.in.dto.GuestMergeStatus;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginResult;
import com.dochiri.authservice.domain.AuthAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoLoginService implements KakaoLoginUseCase {

    private final SocialAccountProvisioner socialAccountProvisioner;
    private final GuestHabitMigrationCoordinator guestHabitMigrationCoordinator;
    private final AuthTokenIssueUseCase authTokenIssueUseCase;

    @Override
    public KakaoLoginResult execute(KakaoLoginCommand command) {
        AuthAccount authAccount = socialAccountProvisioner.authenticateKakao(command.code());
        GuestMergeStatus guestMerge = guestHabitMigrationCoordinator.migrate(
                command.guestSessionToken(),
                authAccount.publicId()
        );
        IssueAuthTokenResult tokens = authTokenIssueUseCase.execute(
                new IssueAuthTokenCommand(authAccount.publicId(), authAccount.role())
        );
        return new KakaoLoginResult(tokens, guestMerge);
    }
}
