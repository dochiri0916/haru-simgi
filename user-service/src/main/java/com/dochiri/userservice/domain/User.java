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
    private final String email;
    private final String nickname;
    private final String profileImageUrl;

    public static User create(String email) {
        return new User(
                generatePublicId(),
                requireNonNull(email),
                null,
                null
        );
    }

    public static User createSocial(String email, String nickname, String profileImageUrl) {
        return new User(
                generatePublicId(),
                email,
                nickname,
                profileImageUrl
        );
    }

    public static User from(String publicId, String email, String nickname, String profileImageUrl) {
        return new User(
                requireNonNull(publicId),
                email,
                nickname,
                profileImageUrl
        );
    }

    private static String generatePublicId() {
        return UUID.randomUUID().toString();
    }

}
