package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.AuthTokenIssueUseCase;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.out.AuthSessionRepository;
import com.dochiri.authservice.application.port.out.TokenGeneratePort;
import com.dochiri.authservice.application.port.out.dto.IssuedTokenResult;
import com.dochiri.authservice.domain.AuthSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenIssuerService implements AuthTokenIssueUseCase {

    private final TokenGeneratePort tokenGeneratePort;
    private final AuthSessionRepository authSessionRepository;

    @Override
    public IssueAuthTokenResult issue(IssueAuthTokenCommand command) {
        IssuedTokenResult tokenResult = tokenGeneratePort.generate(command.publicId(), command.role().name());

        AuthSession authSession = AuthSession.create(
                tokenResult.refreshTokenId(),
                command.userId(),
                command.publicId(),
                command.role(),
                tokenResult.refreshTokenExpiresAt()
        );
        authSessionRepository.saveReplacingUserSessions(authSession);

        return new IssueAuthTokenResult(
                tokenResult.accessToken(),
                tokenResult.refreshToken(),
                tokenResult.refreshTokenExpiresAt(),
                command.role()
        );
    }

}
