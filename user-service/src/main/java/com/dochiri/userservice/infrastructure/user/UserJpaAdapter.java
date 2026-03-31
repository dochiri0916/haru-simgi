package com.dochiri.userservice.infrastructure.user;

import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserJpaAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public User save(User user) {
        Optional<UserEntity> existingEntity = findExistingEntity(user);

        if (existingEntity.isPresent()) {
            UserEntity entity = existingEntity.get();
            validateUpsertCandidate(user, entity);
            entity.updatePasswordHash(user.getPasswordHash());
            return userMapper.toDomain(userJpaRepository.save(entity));
        }

        UserEntity savedEntity = userJpaRepository.save(userMapper.toNewEntity(user));
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public User loadById(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found. id=" + id));
    }

    private Optional<UserEntity> findExistingEntity(User user) {
        if (user.getPublicId() != null) {
            return userJpaRepository.findByPublicId(user.getPublicId())
                    .or(() -> userJpaRepository.findByEmail(user.getEmail()));
        }
        return userJpaRepository.findByEmail(user.getEmail());
    }

    private void validateUpsertCandidate(User user, UserEntity entity) {
        if (!entity.getEmail().equals(user.getEmail())) {
            throw new IllegalArgumentException("User email cannot be changed.");
        }

        if (user.getPublicId() != null && !entity.getPublicId().equals(user.getPublicId())) {
            throw new IllegalArgumentException("User publicId does not match persisted user.");
        }
    }

}