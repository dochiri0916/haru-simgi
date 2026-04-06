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

    public static User create(String email) {
        return new User(
                generatePublicId(),
                requireNonNull(email)
        );
    }

    public static User from(String publicId, String email) {
        return new User(
                requireNonNull(publicId),
                requireNonNull(email)
        );
    }

    private static String generatePublicId() {
        return UUID.randomUUID().toString();
    }

}
