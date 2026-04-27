package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import com.dochiri.authservice.domain.GuestSessionStatus;
import com.dochiri.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

@Entity
@Table(
        name = "guest_sessions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_guest_sessions_public_id", columnNames = "publicId"),
                @UniqueConstraint(name = "uk_guest_sessions_token_hash", columnNames = "tokenHash")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuestSessionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 36)
    private String publicId;

    @Column(nullable = false, updatable = false, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GuestSessionStatus status;

    @Column(nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(nullable = false)
    private Instant lastSeenAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(length = 36)
    private String linkedUserPublicId;

    private Instant linkedAt;

    public GuestSessionEntity(
            String publicId,
            String tokenHash,
            GuestSessionStatus status,
            Instant issuedAt,
            Instant lastSeenAt,
            Instant expiresAt,
            String linkedUserPublicId,
            Instant linkedAt
    ) {
        this.publicId = requireNonNull(publicId);
        this.tokenHash = requireNonNull(tokenHash);
        this.status = requireNonNull(status);
        this.issuedAt = requireNonNull(issuedAt);
        this.lastSeenAt = requireNonNull(lastSeenAt);
        this.expiresAt = requireNonNull(expiresAt);
        this.linkedUserPublicId = linkedUserPublicId;
        this.linkedAt = linkedAt;
    }

    public void apply(
            GuestSessionStatus status,
            Instant lastSeenAt,
            Instant expiresAt,
            String linkedUserPublicId,
            Instant linkedAt
    ) {
        this.status = requireNonNull(status);
        this.lastSeenAt = requireNonNull(lastSeenAt);
        this.expiresAt = requireNonNull(expiresAt);
        this.linkedUserPublicId = linkedUserPublicId;
        this.linkedAt = linkedAt;
    }
}
