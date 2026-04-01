package com.dochiri.userservice.application;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.userservice.application.event.UserRegisteredEvent;
import com.dochiri.userservice.application.port.in.RegisterUserUseCase;
import com.dochiri.userservice.application.port.in.dto.RegisterUserCommand;
import com.dochiri.userservice.application.port.in.dto.RegisterUserResult;
import com.dochiri.userservice.application.port.out.UserEventPublisher;
import com.dochiri.userservice.application.port.out.UserProjection;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import com.dochiri.userservice.presentation.error.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    @Transactional
    @Override
    public RegisterUserResult register(RegisterUserCommand command) {
        validateDuplicateEmail(command.email());

        String passwordHash = passwordEncoder.encode(command.password());
        User newUser = User.create(command.email(), passwordHash);

        try {
            User saved = userRepository.save(newUser);
            UserProjection projection = userRepository.loadProjectionByEmail(saved.getEmail());
            userEventPublisher.publishUserRegistered(new UserRegisteredEvent(
                    projection.userId(),
                    projection.publicId(),
                    projection.email(),
                    projection.passwordHash(),
                    projection.role()
            ));
            return new RegisterUserResult(saved.getId().value(), saved.getEmail());
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
