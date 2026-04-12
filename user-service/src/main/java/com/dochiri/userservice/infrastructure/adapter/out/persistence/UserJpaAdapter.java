package com.dochiri.userservice.infrastructure.adapter.out.persistence;

import com.dochiri.userservice.domain.exception.UserNotFoundException;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Long save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        UserEntity saved = userJpaRepository.save(entity);
        return saved.getId();
    }

    @Override
    public User loadByUserId(Long userId) {
        return userJpaRepository.findById(userId)
                .map(UserMapper::toDomain)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

}