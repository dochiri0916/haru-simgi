package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.AuthenticateUseCase;
import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.LoginCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.RefreshToken;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.jwt.JwtProvider;
import com.dochiri.security.jwt.JwtTokenGenerator;
import com.dochiri.security.jwt.JwtTokenResult;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticateService implements AuthenticateUseCase {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Override
    public AuthTokenResult authenticate(LoginCommand command) {
        AuthAccount account = authAccountRepository.loadByEmail(command.email());

        if (!passwordEncoder.matches(command.password(), account.passwordHash())) {
            throw new BaseException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        JwtTokenResult tokenResult = jwtTokenGenerator.generateToken(account.userId(), account.role().name());
        storeRefreshToken(tokenResult);

        return AuthTokenResult.from(tokenResult, account.role());
    }

    private void storeRefreshToken(JwtTokenResult tokenResult) {
        Claims claims = jwtProvider.parseAndValidate(tokenResult.refreshToken());
        RefreshToken refreshToken = RefreshToken.create(
                jwtProvider.extractTokenId(claims),
                jwtProvider.extractUserId(claims),
                jwtProvider.extractExpiration(claims)
        );

        refreshTokenRepository.replaceByUserId(refreshToken);
    }

}
