package com.dochiri.userservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.userservice.application.error.UserErrorCode;
import com.dochiri.userservice.application.port.in.CreateUserUseCase;
import com.dochiri.userservice.application.port.in.dto.CreateUserCommand;
import com.dochiri.userservice.application.port.in.dto.CreateUserResult;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public CreateUserResult create(CreateUserCommand command) {
        try {
            Long userId = userRepository.create(User.create(command.email()));
            return new CreateUserResult(userId, command.email());
        } catch (DataIntegrityViolationException exception) {
            throw new BaseException(UserErrorCode.DUPLICATE_EMAIL, exception);
        }
    }

}
