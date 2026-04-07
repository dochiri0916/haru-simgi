package com.dochiri.userservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.userservice.application.error.UserErrorCode;
import com.dochiri.userservice.application.port.in.ProvisionUserUseCase;
import com.dochiri.userservice.application.port.in.dto.ProvisionUserCommand;
import com.dochiri.userservice.application.port.in.dto.ProvisionUserResult;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProvisionUserService implements ProvisionUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public ProvisionUserResult provision(ProvisionUserCommand command) {
        try {
            Long userId = userRepository.create(User.create(command.email()));
            return new ProvisionUserResult(userId, command.email());
        } catch (DataIntegrityViolationException exception) {
            throw new BaseException(UserErrorCode.DUPLICATE_EMAIL, exception);
        }
    }

}
