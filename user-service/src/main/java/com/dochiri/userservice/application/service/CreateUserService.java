package com.dochiri.userservice.application.service;

import com.dochiri.userservice.application.port.in.CreateSocialUserUseCase;
import com.dochiri.userservice.application.port.in.dto.CreateSocialUserCommand;
import com.dochiri.userservice.application.port.in.dto.CreateSocialUserResult;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSocialUserService implements CreateSocialUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public CreateSocialUserResult create(CreateSocialUserCommand command) {
        User user = User.createSocial(command.nickname(), command.profileImageUrl());
        Long userId = userRepository.save(user);

        return new CreateSocialUserResult(userId, user.getNickname(), user.getProfileImageUrl());
    }

}