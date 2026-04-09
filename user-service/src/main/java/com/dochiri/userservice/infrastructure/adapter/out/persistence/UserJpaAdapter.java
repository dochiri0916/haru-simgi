package com.dochiri.userservice.infrastructure.adapter.out.persistence;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.userservice.application.error.UserErrorCode;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public Long create(User user) {
        UserEntity saved = userJpaRepository.saveAndFlush(userMapper.toEntity(user));
        return saved.getId();
    }

    @Override
    public Optional<User> findByPublicId(String publicId) {
        return userJpaRepository.findByPublicId(publicId)
                .map(userMapper::toDomain);
    }

    @Override
    public User loadByPublicId(String publicId) {
        return findByPublicId(publicId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User loadByUserId(Long userId) {
        return userJpaRepository.findById(userId)
                .map(userMapper::toDomain)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
    }

}
