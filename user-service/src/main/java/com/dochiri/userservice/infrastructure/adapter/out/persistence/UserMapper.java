package com.dochiri.userservice.infrastructure.adapter.out.persistence;

import com.dochiri.userservice.domain.User;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        requireNonNull(entity);
        return User.from(
                entity.getId(),
                entity.getNickname(),
                entity.getProfileImageUrl()
        );
    }

    public UserEntity toEntity(User domain) {
        requireNonNull(domain);
        return UserEntity.from(
                domain.getId(),
                domain.getNickname(),
                domain.getProfileImageUrl()
        );
    }

}