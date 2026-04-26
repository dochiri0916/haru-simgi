package com.dochiri.userservice.infrastructure.adapter.out.persistence;

import com.dochiri.userservice.domain.Nickname;
import com.dochiri.userservice.domain.ProfileImageUrl;
import com.dochiri.userservice.domain.User;
import com.dochiri.userservice.domain.UserId;

public class UserMapper {

    public static UserEntity toEntity(User domain) {
        return new UserEntity(
                domain.getId().value(),
                domain.getNickname().value(),
                domain.getProfileImageUrl().value()
        );
    }

    public static User toDomain(UserEntity entity) {
        return User.from(
                UserId.of(entity.getPublicId()),
                Nickname.of(entity.getNickname()),
                ProfileImageUrl.of(entity.getProfileImageUrl())
        );
    }

}
