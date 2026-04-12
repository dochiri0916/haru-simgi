package com.dochiri.userservice.application.service;

import com.dochiri.userservice.application.port.in.GetCurrentUserUseCase;
import com.dochiri.userservice.application.port.in.dto.GetCurrentUserResult;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public GetCurrentUserResult getCurrentUser(Long userId) {
        User user = userRepository.loadByUserId(userId);
        return new GetCurrentUserResult(
                user.getId().value(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

}