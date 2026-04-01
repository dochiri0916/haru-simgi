package com.dochiri.userservice.infrastructure.user;

import com.dochiri.userservice.domain.User;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        requireNonNull(entity);
        return User.from(entity.getPublicId(), entity.getEmail(), entity.getPasswordHash());
    }

    public UserEntity toEntity(User domain) {
        requireNonNull(domain);
        return UserEntity.from(domain.getId().value(), domain.getEmail(), domain.getPasswordHash());
    }

    public void applyFullUpdate(User domain, UserEntity entity) {
        requireNonNull(domain);
        requireNonNull(entity);

        if (!entity.getPublicId().equals(domain.getId().value())) {
            throw new IllegalArgumentException("User publicId does not match persisted user.");
        }

        if (!entity.getEmail().equals(domain.getEmail())) {
            throw new IllegalArgumentException("User email cannot be changed.");
        }

        entity.updatePasswordHash(domain.getPasswordHash());
    }

}
