package com.dochiri.userservice.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class User {

    private final String publicId;
    private final String nickname;
    private final String profileImageUrl;

    public static User createSocial(String nickname, String profileImageUrl) {
        return new User(
                generatePublicId(),
                nickname,
                profileImageUrl
        );
    }

    public static User from(String publicId, String nickname, String profileImageUrl) {
        return new User(
                requireNonNull(publicId),
                nickname,
                profileImageUrl
        );
    }

    private static String generatePublicId() {
        return UUID.randomUUID().toString();
    }

}
