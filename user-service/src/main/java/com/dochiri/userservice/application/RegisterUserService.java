package com.dochiri.userservice.application;

import com.dochiri.userservice.application.port.in.RegisterUserUseCase;
import com.dochiri.userservice.application.port.in.dto.RegisterUserCommand;
import com.dochiri.userservice.application.port.in.dto.RegisterUserResult;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterUserResult register(RegisterUserCommand command) {
        User savedUser = userRepository.save(
                User.create(command.email(), passwordEncoder.encode(command.password()))
        );

        return new RegisterUserResult(savedUser.getPublicId(), savedUser.getEmail());
    }

}
