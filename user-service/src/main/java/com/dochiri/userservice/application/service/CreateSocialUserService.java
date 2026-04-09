package com.dochiri.userservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.userservice.application.error.UserErrorCode;
import com.dochiri.userservice.application.port.in.CreateSocialUserUseCase;
import com.dochiri.userservice.application.port.in.dto.CreateSocialUserCommand;
import com.dochiri.userservice.application.port.in.dto.CreateSocialUserResult;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CreateSocialUserService implements CreateSocialUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public CreateSocialUserResult create(CreateSocialUserCommand command) {
        if (!StringUtils.hasText(command.email())) {
            Long userId = userRepository.create(User.createSocial(
                    null,
                    command.nickname(),
                    command.profileImageUrl()
            ));
            return new CreateSocialUserResult(
                    userId,
                    null,
                    command.nickname(),
                    command.profileImageUrl()
            );
        }

        return userRepository.findIdByEmail(command.email())
                .map(userId -> new CreateSocialUserResult(
                        userId,
                        command.email(),
                        command.nickname(),
                        command.profileImageUrl()
                ))
                .orElseGet(() -> createOrLoad(command));
    }

    private CreateSocialUserResult createOrLoad(CreateSocialUserCommand command) {
        try {
            Long userId = userRepository.create(User.createSocial(
                    command.email(),
                    command.nickname(),
                    command.profileImageUrl()
            ));
            return new CreateSocialUserResult(
                    userId,
                    command.email(),
                    command.nickname(),
                    command.profileImageUrl()
            );
        } catch (DataIntegrityViolationException exception) {
            Long userId = userRepository.findIdByEmail(command.email())
                    .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND, exception));
            return new CreateSocialUserResult(
                    userId,
                    command.email(),
                    command.nickname(),
                    command.profileImageUrl()
            );
        }
    }

}
