package com.dochiri.userservice.application.command.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.userservice.application.error.UserErrorCode;
import com.dochiri.userservice.application.command.event.UserRegisteredEvent;
import com.dochiri.userservice.application.command.port.in.RegisterUserUseCase;
import com.dochiri.userservice.application.command.port.in.dto.RegisterUserCommand;
import com.dochiri.userservice.application.command.port.in.dto.RegisterUserResult;
import com.dochiri.userservice.application.command.port.out.UserProjection;
import com.dochiri.userservice.application.command.port.out.UserEventPublisher;
import com.dochiri.userservice.application.command.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    @Transactional
    @Override
    public RegisterUserResult register(RegisterUserCommand command) {
        validateDuplicateEmail(command.email());

        User newUser = User.create(command.email());

        try {
            User saved = userRepository.save(newUser);
            UserProjection projection = userRepository.loadProjectionByEmail(saved.getEmail());
            userEventPublisher.publishUserRegistered(
                    new UserRegisteredEvent(
                            projection.userId(),
                            projection.publicId(),
                            projection.email(),
                            command.password(),
                            projection.role()
                    )
            );
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
