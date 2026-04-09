package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.AuthenticateUseCase;
import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.LoginCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.errorhandling.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticateService implements AuthenticateUseCase {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenIssuer authTokenIssuer;

    @Transactional
    @Override
    public AuthTokenResult authenticate(LoginCommand command) {
        AuthAccount account = authAccountRepository.loadByEmail(command.email());

        if (!passwordEncoder.matches(command.password(), account.passwordHash())) {
            throw new BaseException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        return authTokenIssuer.issue(account);
    }

}