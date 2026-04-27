package com.dochiri.userservice.application.service;

import com.dochiri.userservice.application.port.in.CreateUserUseCase;
import com.dochiri.userservice.application.port.in.dto.CreateUserCommand;
import com.dochiri.userservice.application.port.in.dto.CreateUserResult;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public CreateUserResult execute(CreateUserCommand command) {
        Optional<User> existing = userRepository.findByIdempotencyKey(command.idempotencyKey());
        if (existing.isPresent()) {
            return toResult(existing.get());
        }

        try {
            User saved = userRepository.save(
                    User.create(command.nickname(), command.profileImageUrl()),
                    command.idempotencyKey()
            );
            return toResult(saved);
        } catch (DataIntegrityViolationException exception) {
            return userRepository.findByIdempotencyKey(command.idempotencyKey())
                    .map(this::toResult)
                    .orElseThrow(() -> exception);
        }
    }

    private CreateUserResult toResult(User user) {
        return new CreateUserResult(
                user.getId().value(),
                user.getNickname().value(),
                user.getProfileImageUrl().value()
        );
    }

}
