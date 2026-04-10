package com.dochiri.authservice.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RefreshToken {

    private final String tokenId;
    private final Long userId;
    private final Instant expiresAt;

    public static RefreshToken create(String tokenId, Long userId, Instant expiresAt) {
        return new RefreshToken(
                requireNonNull(tokenId),
                requireNonNull(userId),
                requireNonNull(expiresAt)
        );
    }

}