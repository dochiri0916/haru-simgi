package com.dochiri.userservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.role.UserRole;
import com.dochiri.userservice.application.error.UserErrorCode;
import com.dochiri.userservice.application.port.in.RegisterUserUseCase;
import com.dochiri.userservice.application.port.in.dto.RegisterUserCommand;
import com.dochiri.userservice.application.port.in.dto.RegisterUserResult;
import com.dochiri.userservice.application.port.out.AuthAccountProvisioner;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final AuthAccountProvisioner authAccountProvisioner;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public RegisterUserResult register(RegisterUserCommand command) {
        validateDuplicateEmail(command.email());

        User newUser = User.create(command.email());
        String passwordHash = passwordEncoder.encode(command.password());

        try {
            Long userId = userRepository.save(newUser);
            authAccountProvisioner.provision(userId, newUser.getPublicId(), newUser.getEmail(), passwordHash, UserRole.USER);
            return new RegisterUserResult(newUser.getPublicId(), newUser.getEmail(), UserRole.USER);
        } catch (DataIntegrityViolationException exception) {
            throw new BaseException(UserErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BaseException(UserErrorCode.DUPLICATE_EMAIL);
        }
    }

}
