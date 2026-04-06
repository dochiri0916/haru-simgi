package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.SyncAuthUserUseCase;
import com.dochiri.authservice.application.port.in.dto.SyncAuthUserCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.domain.AuthAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncAuthUserService implements SyncAuthUserUseCase {

    private final AuthAccountRepository authAccountRepository;

    @Transactional
    @Override
    public void sync(SyncAuthUserCommand command) {
        authAccountRepository.upsertByUserId(new AuthAccount(
                command.userId(),
                command.publicId(),
                command.email(),
                command.passwordHash(),
                command.role()
        ));
    }

}
