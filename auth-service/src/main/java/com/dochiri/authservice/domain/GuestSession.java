package com.dochiri.authservice.domain;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public record GuestSession(
        String publicId,
        String tokenHash,
        GuestSessionStatus status,
        Instant createdAt,
        Instant lastSeenAt,
        Instant expiresAt,
        String linkedUserPublicId,
        Instant linkedAt
) {

    public GuestSession {
        requireNonBlank(publicId, "publicId는 비어 있을 수 없습니다.");
        requireNonBlank(tokenHash, "tokenHash는 비어 있을 수 없습니다.");
        requireNonNull(status, "status는 필수입니다.");
        requireNonNull(createdAt, "createdAt은 필수입니다.");
        requireNonNull(lastSeenAt, "lastSeenAt은 필수입니다.");
        requireNonNull(expiresAt, "expiresAt은 필수입니다.");

        if (linkedUserPublicId != null && linkedUserPublicId.isBlank()) {
            throw new IllegalArgumentException("linkedUserPublicId는 비어 있을 수 없습니다.");
        }
        if (status == GuestSessionStatus.LINKED) {
            requireNonBlank(linkedUserPublicId, "연동된 사용자 publicId는 필수입니다.");
            requireNonNull(linkedAt, "linkedAt은 필수입니다.");
        }
        if (status != GuestSessionStatus.LINKED && linkedAt != null) {
            throw new IllegalArgumentException("연동되지 않은 게스트 세션은 linkedAt을 가질 수 없습니다.");
        }
    }

    public static GuestSession create(String publicId, String tokenHash, Instant now, Instant expiresAt) {
        requireNonNull(now, "now는 필수입니다.");
        requireNonNull(expiresAt, "expiresAt은 필수입니다.");
        if (!expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("expiresAt은 now 이후여야 합니다.");
        }

        return new GuestSession(
                publicId,
                tokenHash,
                GuestSessionStatus.ACTIVE,
                now,
                now,
                expiresAt,
                null,
                null
        );
    }

    public boolean isActiveAt(Instant now) {
        requireNonNull(now, "now는 필수입니다.");
        return status == GuestSessionStatus.ACTIVE && expiresAt.isAfter(now);
    }

    public GuestSession touch(Instant now, Instant expiresAt) {
        requireActive(now);
        requireNonNull(expiresAt, "expiresAt은 필수입니다.");
        if (!expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("expiresAt은 now 이후여야 합니다.");
        }

        return new GuestSession(
                publicId,
                tokenHash,
                status,
                createdAt,
                now,
                expiresAt,
                linkedUserPublicId,
                linkedAt
        );
    }

    public GuestSession linkToUser(String userPublicId, Instant now) {
        requireActive(now);
        requireNonBlank(userPublicId, "userPublicId는 비어 있을 수 없습니다.");

        return new GuestSession(
                publicId,
                tokenHash,
                GuestSessionStatus.LINKED,
                createdAt,
                now,
                expiresAt,
                userPublicId,
                now
        );
    }

    public GuestSession expire(Instant now) {
        requireNonNull(now, "now는 필수입니다.");
        if (status == GuestSessionStatus.LINKED) {
            return this;
        }

        return new GuestSession(
                publicId,
                tokenHash,
                GuestSessionStatus.EXPIRED,
                createdAt,
                now,
                expiresAt,
                linkedUserPublicId,
                linkedAt
        );
    }

    public GuestSession revoke(Instant now) {
        requireNonNull(now, "now는 필수입니다.");
        if (status == GuestSessionStatus.LINKED) {
            return this;
        }

        return new GuestSession(
                publicId,
                tokenHash,
                GuestSessionStatus.REVOKED,
                createdAt,
                now,
                expiresAt,
                linkedUserPublicId,
                linkedAt
        );
    }

    private void requireActive(Instant now) {
        if (!isActiveAt(now)) {
            throw new IllegalStateException("활성 게스트 세션이 아닙니다.");
        }
    }

    private static void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
