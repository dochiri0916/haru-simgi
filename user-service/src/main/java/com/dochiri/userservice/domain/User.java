package com.dochiri.userservice.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class User {

    private final Long internalId;
    private final UserId id;
    private final Nickname nickname;
    private final ProfileImageUrl profileImageUrl;

    public static User create(Nickname nickname, ProfileImageUrl profileImageUrl) {
        return new User(
                null,
                UserId.newId(),
                nickname,
                profileImageUrl
        );
    }

    public static User from(Long internalId, UserId id, Nickname nickname, ProfileImageUrl profileImageUrl) {
        return new User(
                internalId,
                id,
                nickname,
                profileImageUrl
        );
    }

    public User changeNickname(Nickname nickname) {
        return new User(this.internalId, this.id, nickname, this.profileImageUrl);
    }

    public User changeProfileImageUrl(ProfileImageUrl profileImageUrl) {
        return new User(this.internalId, this.id, this.nickname, profileImageUrl);
    }

}
