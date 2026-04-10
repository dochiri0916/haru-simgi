package com.dochiri.userservice.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class User {

    private final String id;
    private final String nickname;
    private final String profileImageUrl;

    public static User createSocial(String nickname, String profileImageUrl) {
        return new User(
                generateId(),
                nickname,
                profileImageUrl
        );
    }

    public static User from(String id, String nickname, String profileImageUrl) {
        return new User(
                requireNonNull(id),
                nickname,
                profileImageUrl
        );
    }

    private static String generateId() {
        return UUID.randomUUID().toString();
    }

}