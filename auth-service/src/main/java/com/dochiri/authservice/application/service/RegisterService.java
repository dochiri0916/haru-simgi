package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.RegisterUseCase;
import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.RegisterCommand;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.application.port.out.UserProvisionPort;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.security.role.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {

    private final UserProvisionPort userProvisionPort;
    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenIssuer authTokenIssuer;

    @Transactional
    @Override
    public AuthTokenResult register(RegisterCommand command) {
        var provisionedUser = userProvisionPort.provision(command.email());
        AuthAccount authAccount = authAccountRepository.save(new AuthAccount(
                provisionedUser.userId(),
                AuthProvider.LOCAL,
                null,
                provisionedUser.email(),
                passwordEncoder.encode(command.password()),
                UserRole.USER
        ));

        return authTokenIssuer.issue(authAccount);
    }

}
