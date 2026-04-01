package com.dochiri.userservice.domain;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record Id(
        String value
) {
    public Id {
        requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException("value is blank");
        }
    }

    public static Id from(String value) {
        return new Id(value);
    }

    public static Id newId() {
        return new Id(UUID.randomUUID().toString());
    }
}