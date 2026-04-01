package com.dochiri.userservice.infrastructure.user;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.Id;
import com.dochiri.userservice.domain.User;
import com.dochiri.userservice.presentation.error.UserErrorCode;
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
        Optional<UserEntity> existingOptional = userJpaRepository.findByPublicId(user.getId().value());

        if (existingOptional.isEmpty()) {
            UserEntity newEntity = userMapper.toEntity(user);
            UserEntity saved = userJpaRepository.save(newEntity);
            return userMapper.toDomain(saved);
        }

        UserEntity existing = existingOptional.get();
        userMapper.applyFullUpdate(user, existing);
        return userMapper.toDomain(existing);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findById(Id id) {
        return userJpaRepository.findByPublicId(id.value())
                .map(userMapper::toDomain);
    }

    @Override
    public User loadById(Id id) {
        return findById(id)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

}
