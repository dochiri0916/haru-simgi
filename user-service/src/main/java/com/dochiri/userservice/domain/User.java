package com.dochiri.userservice.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class User {

    private final Long userId;
    private final Id id;
    private final String email;

    public static User create(String email) {
        return new User(
                null,
                Id.newId(),
                requireNonNull(email)
        );
    }

    public static User from(Long userId, String publicId, String email) {
        return new User(
                userId,
                Id.from(publicId),
                requireNonNull(email)
        );
    }

}