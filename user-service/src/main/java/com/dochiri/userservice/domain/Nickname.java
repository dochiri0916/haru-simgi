package com.dochiri.userservice.domain;

import com.dochiri.userservice.domain.exception.InvalidNicknameException;

public record Nickname(
        String value
) {
    private static final int MAX_LENGTH = 100;

    public Nickname {
        validate(value);
    }

    public static Nickname of(String value) {
        return new Nickname(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank() || value.length() > MAX_LENGTH) {
            throw new InvalidNicknameException(value);
        }
    }
}
