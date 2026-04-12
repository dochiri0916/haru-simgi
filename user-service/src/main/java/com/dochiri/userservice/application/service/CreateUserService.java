package com.dochiri.userservice.application.service;

import com.dochiri.userservice.application.port.in.CreateUserUseCase;
import com.dochiri.userservice.application.port.in.dto.CreateUserCommand;
import com.dochiri.userservice.application.port.in.dto.CreateUserResult;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public CreateUserResult execute(CreateUserCommand command) {
        User user = User.create(command.nickname(), command.profileImageUrl());
        Long userId = userRepository.save(user);

        return new CreateUserResult(
                userId,
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

}