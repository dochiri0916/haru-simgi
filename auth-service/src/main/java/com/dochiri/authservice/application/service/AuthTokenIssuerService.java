package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.AuthTokenIssueUseCase;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.application.port.out.TokenGeneratePort;
import com.dochiri.authservice.application.port.out.dto.IssuedTokenResult;
import com.dochiri.authservice.domain.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenIssuerService implements AuthTokenIssueUseCase {

    private final TokenGeneratePort tokenGeneratePort;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public IssueAuthTokenResult issue(IssueAuthTokenCommand command) {
        IssuedTokenResult tokenResult = tokenGeneratePort.generate(command.userId(), command.role().name());

        RefreshToken refreshToken = RefreshToken.create(
                tokenResult.refreshTokenId(),
                command.userId(),
                tokenResult.refreshTokenExpiresAt()
        );
        refreshTokenRepository.replaceByUserId(refreshToken);

        return new IssueAuthTokenResult(
                tokenResult.accessToken(),
                tokenResult.refreshToken(),
                tokenResult.refreshTokenExpiresAt(),
                command.role()
        );
    }

}