package com.dochiri.userservice.infrastructure.user;

import com.dochiri.userservice.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        return User.restore(entity.getPublicId(), entity.getEmail(), entity.getPasswordHash());
    }

    public UserEntity toNewEntity(User user) {
        return UserEntity.from(user.getPublicId(), user.getEmail(), user.getPasswordHash());
    }

}