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
    public User save(User user) {
        Optional<UserEntity> existingOptional = userJpaRepository.findByPublicId(user.getId());

        if (existingOptional.isEmpty()) {
            UserEntity newEntity = userMapper.toEntity(user);
            UserEntity saved = userJpaRepository.save(newEntity);
            return userMapper.toDomain(saved);
        }

        return userMapper.toDomain(existingOptional.get());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findById(String publicId) {
        return userJpaRepository.findByPublicId(publicId)
                .map(userMapper::toDomain);
    }

    @Override
    public User loadById(String publicId) {
        return findById(publicId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

}
