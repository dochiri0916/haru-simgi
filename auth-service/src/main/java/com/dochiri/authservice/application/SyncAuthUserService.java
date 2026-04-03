package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.SyncAuthUserUseCase;
import com.dochiri.authservice.application.port.in.dto.SyncAuthUserCommand;
import com.dochiri.authservice.application.port.out.AuthUserRepository;
import com.dochiri.authservice.domain.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncAuthUserService implements SyncAuthUserUseCase {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void sync(SyncAuthUserCommand command) {
        authUserRepository.save(new AuthUser(
                command.userId(),
                command.publicId(),
                command.email(),
                passwordEncoder.encode(command.password()),
                command.role()
        ));
    }
}
