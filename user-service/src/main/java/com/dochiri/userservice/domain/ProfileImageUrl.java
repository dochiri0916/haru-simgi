package com.dochiri.userservice.domain;

import com.dochiri.userservice.domain.exception.InvalidProfileImageUrlException;

public record ProfileImageUrl(
        String value
) {
    private static final int MAX_LENGTH = 500;

    public ProfileImageUrl {
        validate(value);
    }

    public static ProfileImageUrl of(String value) {
        return new ProfileImageUrl(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank() || value.length() > MAX_LENGTH) {
            throw new InvalidProfileImageUrlException(value);
        }
    }
}
