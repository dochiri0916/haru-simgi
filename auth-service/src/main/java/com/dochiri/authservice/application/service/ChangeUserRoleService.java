package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.in.ChangeUserRoleUseCase;
import com.dochiri.authservice.application.port.in.dto.ChangeUserRoleCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.errorhandling.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeUserRoleService implements ChangeUserRoleUseCase {

    private final AuthAccountRepository authAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Override
    public void changeRole(ChangeUserRoleCommand command) {
        AuthAccount account = authAccountRepository.findByUserId(command.userId())
                .orElseThrow(() -> new BaseException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND));

        authAccountRepository.upsertByUserId(new AuthAccount(
                account.userId(),
                account.publicId(),
                account.email(),
                account.passwordHash(),
                command.role()
        ));

        refreshTokenRepository.deleteByUserId(command.userId());
    }
}
