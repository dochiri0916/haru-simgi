package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.in.AuthTokenIssueUseCase;
import com.dochiri.authservice.application.port.in.ReissueTokenUseCase;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.application.port.out.TokenParsePort;
import com.dochiri.authservice.application.port.out.dto.ParseRefreshTokenResult;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.RefreshToken;
import com.dochiri.errorhandling.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReissueTokenService implements ReissueTokenUseCase {

    private final TokenParsePort tokenParsePort;
    private final AuthTokenIssueUseCase authTokenIssueUseCase;
    private final AuthAccountRepository authAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Override
    public IssueAuthTokenResult reissue(RefreshTokenCommand command) {
        ParseRefreshTokenResult parsed = tokenParsePort.parseRefreshToken(command.refreshToken());

        RefreshToken storedRefreshToken = refreshTokenRepository.findByTokenId(parsed.tokenId())
                .orElseThrow(() -> new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedRefreshToken.getUserId().equals(parsed.userId())) {
            throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        AuthAccount account = authAccountRepository.findByUserId(parsed.userId())
                .orElseThrow(() -> new BaseException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND));

        return authTokenIssueUseCase.issue(new IssueAuthTokenCommand(account.userId(), account.role()));
    }
}
