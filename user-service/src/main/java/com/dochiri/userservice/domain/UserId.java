package com.dochiri.userservice.domain;

import com.dochiri.userservice.domain.exception.InvalidUserIdException;

import java.util.UUID;

public record UserId(
        String value
) {
    public UserId {
        validate(value);
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserIdException(value);
        }

        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidUserIdException(value);
        }
    }
}
