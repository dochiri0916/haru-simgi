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
    private final String passwordHash;

    public static User create(String email, String passwordHash) {
        return new User(
                UUID.randomUUID().toString(),
                requireNonNull(email),
                requireNonNull(passwordHash)
        );
    }

    public static User restore(String publicId, String email, String passwordHash) {
        return new User(
                requireNonNull(publicId),
                requireNonNull(email),
                requireNonNull(passwordHash)
        );
    }

}