package com.dochiri.userservice.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class User {

    private final UserId id;
    private final String nickname;
    private final String profileImageUrl;

    public static User create(String nickname, String profileImageUrl) {
        return new User(
                UserId.newId(),
                nickname,
                profileImageUrl
        );
    }

    public static User from(UserId id, String nickname, String profileImageUrl) {
        return new User(
                id,
                nickname,
                profileImageUrl
        );
    }

}