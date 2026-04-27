package com.dochiri.userservice.infrastructure.adapter.out.persistence;

import com.dochiri.userservice.domain.exception.UserNotFoundException;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user, String idempotencyKey) {
        UserEntity entity = UserMapper.toEntity(user, idempotencyKey);
        UserEntity saved = userJpaRepository.save(entity);
        return UserMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return Optional.empty();
        }
        return userJpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(UserMapper::toDomain);
    }

    @Override
    public User loadById(String publicId) {
        return userJpaRepository.findByPublicId(publicId)
                .map(UserMapper::toDomain)
                .orElseThrow(() -> new UserNotFoundException(publicId));
    }

}
