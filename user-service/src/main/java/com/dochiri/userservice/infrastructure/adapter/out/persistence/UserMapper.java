package com.dochiri.userservice.infrastructure.adapter.out.persistence;

import com.dochiri.userservice.domain.User;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        requireNonNull(entity);
        return User.from(
                entity.getPublicId(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getProfileImageUrl()
        );
    }

    public UserEntity toEntity(User domain) {
        requireNonNull(domain);
        return UserEntity.from(
                domain.getPublicId(),
                domain.getEmail(),
                domain.getNickname(),
                domain.getProfileImageUrl()
        );
    }

}
